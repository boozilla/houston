package boozilla.houston.asset;

import boozilla.houston.asset.codec.AssetLinkCodec;
import boozilla.houston.asset.codec.ProtobufRowCodec;
import boozilla.houston.asset.constraints.AssetAccessor;
import boozilla.houston.asset.sql.SqlStatement;
import boozilla.houston.container.AssetQuery;
import boozilla.houston.container.Query;
import boozilla.houston.entity.Data;
import boozilla.houston.repository.Vaults;
import com.google.protobuf.*;
import houston.grpc.service.AssetSchema;
import houston.grpc.service.AssetSheet;
import houston.vo.asset.Archive;
import houston.vo.asset.NullableFields;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

public class AssetContainer implements AssetAccessor {
    private final Vaults vaults;
    private final Map<AssetAccessor.Key, Data> data;
    private final Map<AssetAccessor.Key, Archive> archive;
    private final Map<AssetAccessor.Key, ProtobufRowCodec> codec;
    private final Map<AssetAccessor.Key, AssetQuery> query;
    private final Map<String, Set<AssetLink>> links;
    private final Map<String, Set<String>> partitions;
    private final Set<AssetAccessor.Key> updated;

    AssetContainer(final Vaults vaults)
    {
        this.vaults = vaults;
        this.data = new HashMap<>();
        this.archive = new HashMap<>();
        this.codec = new HashMap<>();
        this.query = new HashMap<>();
        this.links = new HashMap<>();
        this.partitions = new HashMap<>();
        this.updated = new HashSet<>();
    }

    Set<Key> keys()
    {
        return this.data.keySet();
    }

    boolean different(final Data data)
    {
        final var key = new AssetAccessor.Key(data.getSheetName(), data.getPartitionName(), data.getScope());
        final var currentData = this.data.get(key);

        if(Objects.isNull(currentData))
            return true;

        return !currentData.getCommitId().contentEquals(data.getCommitId());
    }

    public Mono<AssetContainer> add(final Data data, final Archive archive)
    {
        final var key = data.key();
        remove(key);

        this.data.put(key, data);
        this.archive.put(key, archive);
        this.updated.add(key);

        final var linkCodec = new AssetLinkCodec();
        final var links = linkCodec.deserialize(archive.getLinkBytes().toByteArray())
                .stream()
                .map(AssetLink::new)
                .collect(Collectors.toSet());

        if(!links.isEmpty())
            this.links.put(data.getSheetName(), links);

        final var partitionList = partitions.getOrDefault(key.sheetName(), new HashSet<>());
        key.partition().ifPresent(partitionList::add);

        partitions.put(key.sheetName(), partitionList);

        try
        {
            final var nullableFields = nullableFields(archive);
            final var fileDescriptorProto = DescriptorProtos.FileDescriptorProto.parseFrom(archive.getDescriptorBytes().toByteArray());
            final var fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, new Descriptors.FileDescriptor[0]);
            final var rowCodec = new ProtobufRowCodec(data.getSheetName(), fileDescriptor, nullableFields);

            this.codec.put(key.toMergeKey(), rowCodec);
        }
        catch(InvalidProtocolBufferException | Descriptors.DescriptorValidationException e)
        {
            return Mono.error(new RuntimeException("Errors initialize the protobuf data codec", e));
        }

        return Mono.just(this);
    }

    public Mono<AssetContainer> addAll(final Collection<Tuple2<Data, Archive>> updatedData)
    {
        return Flux.fromIterable(updatedData)
                .flatMap(tuple -> add(tuple.getT1(), tuple.getT2()))
                .then(Mono.just(this));
    }

    public void remove(final AssetAccessor.Key key)
    {
        this.data.remove(key);
        this.archive.remove(key);
        this.codec.remove(key.toMergeKey());
        this.query.remove(key.toMergeKey());
        this.links.remove(key.sheetName());
        this.partitions.remove(key.sheetName());
        this.updated.remove(key);
    }

    public AssetContainer copy()
    {
        final var copy = new AssetContainer(vaults);
        copy.data.putAll(data);
        copy.archive.putAll(archive);
        copy.codec.putAll(codec);
        copy.query.putAll(query);
        copy.links.putAll(links);
        copy.partitions.putAll(partitions);

        return copy;
    }

    public JavaType columnType(final String sheetName, final String columnName)
    {
        final var scope = columnScope(sheetName, columnName).stream()
                .findAny()
                .orElseThrow();

        return this.codec.get(new AssetAccessor.Key(sheetName, scope)).getFieldDescriptor()
                .stream()
                .filter(fieldDescriptor -> fieldDescriptor.getName().contentEquals(columnName))
                .findAny()
                .map(fieldDescriptor -> JavaType.valueOf(fieldDescriptor.getJavaType().name()))
                .orElse(JavaType.VOID);
    }

    public Set<Scope> columnScope(final String sheetName, final String columnName)
    {
        return Arrays.stream(Scope.values())
                .filter(scope -> {
                    final var codec = this.codec.get(new AssetAccessor.Key(sheetName, scope));

                    return Objects.nonNull(codec) && codec.getFieldDescriptor().stream()
                            .anyMatch(field -> field.getName().contentEquals(columnName));
                })
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<Key> updatedKey()
    {
        return updated;
    }

    public Set<Tuple2<Data, Archive>> updatedData()
    {
        return updatedKey().stream()
                .map(key -> Tuples.of(data.get(key), archive.get(key)))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<Key> updatedMergeKey()
    {
        return updated.stream()
                .map(Key::toMergeKey)
                .collect(Collectors.toSet());
    }

    public Set<String> partitions(final String sheetName)
    {
        final var partitions = this.partitions.get(sheetName);
        return Objects.requireNonNullElse(partitions, Set.of());
    }

    public Flux<AssetSheet> list(final Scope scope)
    {
        final var stream = this.data.keySet().stream()
                .filter(key -> key.scope() == scope)
                .map(key -> {
                    final var data = this.data.get(key);
                    final var query = this.query.get(key.toMergeKey());
                    final var codec = this.codec.get(key.toMergeKey());

                    final var fileDescriptor = codec.getFileDescriptor();
                    final var nullableFields = codec.nullableFields();

                    return AssetSheet.newBuilder()
                            .setSize(query.size())
                            .setCommitId(data.getCommitId())
                            .setName(data.getName())
                            .setStructure(fileDescriptor.toProto().toByteString())
                            .setNullableFields(nullableFields.toByteString())
                            .addAllPartition(partitions(key.sheetName()))
                            .build();
                });

        return Flux.fromStream(stream);
    }

    public void initialize()
    {
        this.archive.entrySet().stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().toMergeKey(), Collectors.mapping(Map.Entry::getValue, Collectors.toList())))
                .forEach((mergeKey, archives) -> {
                    if(this.query.containsKey(mergeKey))
                        return;

                    final var codec = this.codec.get(mergeKey);
                    final var sheetDescriptor = codec.getFileDescriptor().findMessageTypeByName(mergeKey.sheetName());
                    final var data = archives.stream().flatMap(archive -> codec.deserialize(archive.getDataBytes().toByteArray()).stream())
                            .map(Any::pack)
                            .toList();

                    final var query = new AssetQuery(data, sheetDescriptor, codec.nullableFields());

                    this.query.put(mergeKey, query);
                });
    }

    private NullableFields nullableFields(final Archive archive)
    {
        try
        {
            return NullableFields.parseFrom(archive.getNullableBytes());
        }
        catch(InvalidProtocolBufferException e)
        {
            throw new RuntimeException("Error creating nullable fields information", e);
        }
    }

    public Flux<AssetLink> links(final String sheetName)
    {
        final var stream = links.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .map(link -> link.related(sheetName))
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet())
                .stream();

        return Flux.fromStream(stream);
    }

    public Flux<AssetSchema> schemas(final Scope scope)
    {
        return Flux.fromIterable(data.keySet())
                .filter(key -> key.scope() == scope)
                .map(key -> {
                    final var archive = this.archive.get(key);
                    return AssetSchema.newBuilder()
                            .setName(key.sheetName())
                            .setSchema(archive.getSchema())
                            .build();
                });
    }

    public Flux<AssetData> query(final AssetLink link, final SqlStatement<?> sql)
    {
        return query(link, sql.toString());
    }

    public Flux<AssetData> query(final Scope scope, final SqlStatement<?> sql)
    {
        return query(scope, sql.toString());
    }

    public Flux<AssetData> query(final AssetLink link, final String sql)
    {
        final var scope = columnScope(link.getSheetName(), link.getColumnName()).stream()
                .findAny()
                .orElseThrow();

        return query(scope, sql);
    }

    public Flux<AssetData> query(final Scope scope, final String sql)
    {
        final var query = Query.of(sql);

        final var mergeKey = new AssetAccessor.Key(query.from(), scope);
        final var codec = this.codec.get(mergeKey);
        final var assetQuery = this.query.get(mergeKey);

        if(Objects.isNull(assetQuery) || Objects.isNull(codec))
            return Flux.empty();

        return query.result(assetQuery, codec.getFieldDescriptor());
    }
}
