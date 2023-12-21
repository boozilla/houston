package boozilla.houston.asset.codec;

import boozilla.houston.Application;
import boozilla.houston.asset.AssetSheet;
import boozilla.houston.exception.AssetSheetException;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistry;
import houston.vo.asset.AssetLink;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssetLinkCodec implements AssetCodec<byte[], AssetLink> {
    @Override
    public byte[] serialize(final AssetSheet sheet)
    {
        try(final var output = new FastByteArrayOutputStream())
        {
            sheet.columnIndices().mapToObj(sheet::column)
                    .filter(column -> Objects.nonNull(column.link()))
                    .forEach(column -> {
                        final var split = column.link().split("\\.", 2);
                        final var sheetName = split[0];
                        final var columnName = split.length > 1 ? split[1] : "code";

                        final var builder = houston.vo.asset.AssetLink.newBuilder()
                                .setSheetName(sheet.sheetName())
                                .setColumnName(column.name())
                                .setRelated(houston.vo.asset.AssetLink.newBuilder()
                                        .setSheetName(sheetName)
                                        .setColumnName(columnName));

                        try
                        {
                            builder.build().writeDelimitedTo(output);
                        }
                        catch(Exception e)
                        {
                            throw new AssetSheetException(
                                    Application.messageSourceAccessor().getMessage("READ_SHEET_ERROR_FAILED_SERIALIZE"),
                                    sheet.name(), e);
                        }
                    });

            return output.toByteArray();
        }
    }

    @Override
    public List<AssetLink> deserialize(final byte[] data)
    {
        final var result = new ArrayList<AssetLink>();
        final var input = CodedInputStream.newInstance(data);

        try
        {
            while(!input.isAtEnd())
            {
                final var assetLink = input.readMessage(AssetLink.parser(), ExtensionRegistry.getEmptyRegistry());
                result.add(assetLink);
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException("Failed to deserialize asset link", e);
        }

        return result;
    }
}
