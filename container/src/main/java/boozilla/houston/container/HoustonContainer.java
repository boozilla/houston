package boozilla.houston.container;

import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.QueryResultInfo;
import boozilla.houston.asset.sql.SqlStatement;
import com.google.protobuf.*;
import houston.grpc.service.AssetSheet;
import houston.grpc.service.Manifest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

@Slf4j
public class HoustonContainer {
    private final Map<String, AssetSheet> sheets;
    private final Map<String, AssetQuery> query;
    private final Map<String, Descriptors.Descriptor> descriptors;
    private final Set<String> updatedSheets;

    HoustonContainer()
    {
        sheets = new ConcurrentHashMap<>();
        query = new ConcurrentHashMap<>();
        descriptors = new ConcurrentHashMap<>();
        updatedSheets = new ConcurrentSkipListSet<>();
    }

    public Flux<AssetData> query(final SqlStatement<?> sql)
    {
        return query(sql.toString(), null);
    }

    public Flux<AssetData> query(final SqlStatement<?> sql, final Consumer<QueryResultInfo> resultInfoConsumer)
    {
        return query(sql.toString(), resultInfoConsumer);
    }

    public Flux<AssetData> query(final String sql)
    {
        return query(sql, null);
    }

    public Flux<AssetData> query(final String sql, final Consumer<QueryResultInfo> resultInfoConsumer)
    {
        final var query = Query.of(sql);

        if(!this.query.containsKey(query.from()))
            return Flux.empty();

        return query.result(
                this.query.get(query.from()),
                descriptors.get(query.from()).getFields(),
                resultInfoConsumer
        );
    }

    void add(final AssetSheet sheet, final List<Any> data)
    {
        remove(sheet);

        final var sheetDescriptor = sheetDescriptor(sheet.getName(), sheet.getStructure());
        final var query = new AssetQuery(sheet.getCommitId(), data, sheetDescriptor);

        this.sheets.put(sheet.getName(), sheet);
        this.query.put(sheet.getName(), query);
        this.descriptors.put(sheet.getName(), sheetDescriptor);
        this.updatedSheets.add(sheet.getName());

        log.info("Asset data loaded [name={}, partitions={}, commitId={}, size={}]",
                sheet.getName(), sheet.getPartitionList(), sheet.getCommitId(), data.size());
    }

    void remove(final AssetSheet sheet)
    {
        if(!this.sheets.containsKey(sheet.getName()))
            return;

        this.sheets.remove(sheet.getName());
        this.query.remove(sheet.getName());
        this.descriptors.remove(sheet.getName());
        this.updatedSheets.remove(sheet.getName());

        log.info("Asset data removed [name={}, partitions={}, commitId={}]",
                sheet.getName(), sheet.getPartitionList(), sheet.getCommitId());
    }

    Set<String> updatedSheets()
    {
        return this.updatedSheets;
    }

    HoustonContainer copy()
    {
        final var newContainer = new HoustonContainer();
        newContainer.sheets.putAll(this.sheets);
        newContainer.query.putAll(this.query);
        newContainer.descriptors.putAll(this.descriptors);

        return newContainer;
    }

    private Descriptors.Descriptor sheetDescriptor(final String name, final ByteString fileDescriptorProtoBytes)
    {
        try
        {
            final var fileDescriptorProto = DescriptorProtos.FileDescriptorProto.parseFrom(fileDescriptorProtoBytes);
            final var fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, new Descriptors.FileDescriptor[0]);

            return fileDescriptor.findMessageTypeByName(name);
        }
        catch(Descriptors.DescriptorValidationException | InvalidProtocolBufferException e)
        {
            throw new RuntimeException("Error creating asset sheet descriptor", e);
        }
    }

    Collection<AssetSheet> sheets()
    {
        return sheets.values();
    }

    String commitId(final String name)
    {
        return sheets.get(name)
                .getCommitId();
    }
}
