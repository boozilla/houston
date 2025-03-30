package boozilla.houston.container;

import boozilla.houston.asset.QueryResultInfo;
import boozilla.houston.utils.MessageUtils;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.attribute.support.SimpleFunction;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radixreversed.ReversedRadixTreeIndex;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.sql.SQLParser;
import com.googlecode.cqengine.resultset.ResultSet;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.nullableAttribute;

public class AssetQuery {
    private final String commitId;
    private final ConcurrentIndexedCollection<DynamicMessage> indexedCollection;
    private final SQLParser<DynamicMessage> sqlParser;

    private SimpleAttribute<DynamicMessage, Long> primary;

    public AssetQuery(final String commitId, final List<Any> data, final Descriptors.Descriptor sheetDescriptor)
    {
        final var attributes = attributes(sheetDescriptor.getFields());
        final var indices = indices(sheetDescriptor.getFields(), attributes);

        this.commitId = commitId;
        this.indexedCollection = indexedCollection(sheetDescriptor, indices, data);
        this.sqlParser = SQLParser.forPojoWithAttributes(DynamicMessage.class, attributes);
    }

    public ResultSet<DynamicMessage> query(final String sql, final Consumer<QueryResultInfo> resultInfoConsumer)
    {
        final var parseResult = this.sqlParser.parse(sql);
        final var resultSet = indexedCollection.retrieve(parseResult.getQuery(), parseResult.getQueryOptions());

        if(Objects.nonNull(resultInfoConsumer))
        {
            final var resultInfo = new QueryResultInfo(
                    commitId,
                    resultSet.size(),
                    resultSet.getMergeCost(),
                    resultSet.getRetrievalCost());
            resultInfoConsumer.accept(resultInfo);
        }

        return resultSet;
    }

    public int size()
    {
        return indexedCollection.size();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Attribute<DynamicMessage, ?>> attributes(final List<Descriptors.FieldDescriptor> fieldDescriptors)
    {
        return fieldDescriptors.stream()
                .<Attribute<DynamicMessage, ?>>map(fieldDescriptor -> {
                    final var attributeName = fieldDescriptor.getName();
                    final var typeClass = (Class<Object>) typeClass(fieldDescriptor);

                    if(fieldDescriptor.isRepeated())
                    {
                        return new MultiValueAttribute<>(DynamicMessage.class, typeClass, attributeName) {
                            @Override
                            public Iterable<Object> getValues(final DynamicMessage dynamicMessage, final QueryOptions queryOptions)
                            {
                                return IntStream.range(0, dynamicMessage.getRepeatedFieldCount(fieldDescriptor))
                                        .mapToObj(index -> dynamicMessage.getRepeatedField(fieldDescriptor, index))
                                        .toList();
                            }
                        };
                    }
                    else if(fieldDescriptor.getName().contentEquals("code"))
                    {
                        primary = new SimpleAttribute<>(attributeName) {
                            @Override
                            public Long getValue(final DynamicMessage dynamicMessage, final QueryOptions queryOptions)
                            {
                                return (Long) dynamicMessage.getField(fieldDescriptor);
                            }
                        };

                        return primary;
                    }

                    final var valueFunc = new SimpleFunction<DynamicMessage, Object>() {
                        @Override
                        public Object apply(final DynamicMessage dynamicMessage)
                        {
                            if(!isNullable(fieldDescriptor) && !dynamicMessage.hasField(fieldDescriptor))
                                return fieldDescriptor.getDefaultValue();
                            else if(dynamicMessage.hasField(fieldDescriptor))
                                return MessageUtils.extractValue(dynamicMessage.getField(fieldDescriptor))
                                        .orElse(null);

                            return null;
                        }
                    };

                    return isNullable(fieldDescriptor) ? nullableAttribute(DynamicMessage.class, typeClass, attributeName, valueFunc) :
                            attribute(DynamicMessage.class, typeClass, attributeName, valueFunc);
                })
                .collect(Collectors.toMap(Attribute::getAttributeName, Function.identity()));
    }

    private boolean isNullable(final Descriptors.FieldDescriptor fieldDescriptor)
    {
        return fieldDescriptor.getType() == Descriptors.FieldDescriptor.Type.MESSAGE;
    }

    private Class<?> typeClass(final Descriptors.FieldDescriptor fieldDescriptor)
    {
        return switch(fieldDescriptor.getJavaType())
        {
            case LONG -> Long.class;
            case INT -> Integer.class;
            case DOUBLE -> Double.class;
            case STRING -> String.class;
            case BOOLEAN -> Boolean.class;
            case MESSAGE -> switch(fieldDescriptor.toProto().getTypeName())
            {
                case "Int64Value" -> Long.class;
                case "Int32Value" -> Integer.class;
                case "DoubleValue" -> Double.class;
                case "StringValue" -> String.class;
                case "BoolValue" -> Boolean.class;
                default -> throw new RuntimeException("Unknown type");
            };
            default -> throw new RuntimeException("Unknown type");
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Index<DynamicMessage>> indices(final List<Descriptors.FieldDescriptor> fieldDescriptors,
                                                final Map<String, Attribute<DynamicMessage, ?>> attributes)
    {
        return fieldDescriptors.stream()
                .<Index<DynamicMessage>>map(fieldDescriptor -> {
                    final var attributeName = fieldDescriptor.getName();
                    final var attribute = attributes.get(attributeName);

                    if(fieldDescriptor.isRepeated())
                    {
                        return HashIndex.onAttribute((Attribute<DynamicMessage, List<?>>) attribute);
                    }

                    if(isNullable(fieldDescriptor))
                    {
                        return HashIndex.onAttribute((Attribute<DynamicMessage, ?>) attribute);
                    }

                    return switch(fieldDescriptor.getJavaType())
                    {
                        case INT, LONG, BOOLEAN, FLOAT, DOUBLE ->
                                NavigableIndex.onAttribute((Attribute<DynamicMessage, ? extends Comparable>) attribute);
                        case STRING ->
                                ReversedRadixTreeIndex.onAttribute((Attribute<DynamicMessage, String>) attribute);
                        default -> HashIndex.onAttribute((Attribute<DynamicMessage, ?>) attribute);
                    };
                })
                .toList();
    }

    private ConcurrentIndexedCollection<DynamicMessage> indexedCollection(final Descriptors.Descriptor sheetDescriptor,
                                                                          final List<Index<DynamicMessage>> indices,
                                                                          final List<Any> data)
    {
        final var persistence = OnHeapPersistence.onPrimaryKey(primary);
        final var indexedCollection = new ConcurrentIndexedCollection<>(persistence);
        indices.forEach(indexedCollection::addIndex);

        final var rows = data.stream()
                .map(any -> {
                    try
                    {
                        return any.unpackSameTypeAs(DynamicMessage.getDefaultInstance(sheetDescriptor));
                    }
                    catch(InvalidProtocolBufferException e)
                    {
                        throw new RuntimeException("An error occurred while indexing asset data", e);
                    }
                })
                .toList();

        indexedCollection.addAll(rows);

        return indexedCollection;
    }
}
