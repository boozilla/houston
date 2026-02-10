package boozilla.houston.mcp.tool;

import boozilla.houston.asset.AssetContainers;
import boozilla.houston.mcp.McpToolProvider;
import boozilla.houston.mcp.McpToolUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "armeria.mcp.enabled", havingValue = "true")
@AllArgsConstructor
public class AssetSchemaTool implements McpToolProvider {
    private final AssetContainers assets;
    private final ObjectMapper objectMapper;

    @Override
    public List<McpServerFeatures.AsyncToolSpecification> tools()
    {
        final var inputSchema = new McpSchema.JsonSchema(
                "object",
                Map.of(
                        "scope", McpToolUtils.SCOPE_SCHEMA
                ),
                List.of(),
                false,
                null,
                null
        );

        final var tool = McpSchema.Tool.builder()
                .name("get_asset_schema")
                .description("Get schema definitions for asset data tables. Returns column names and types for each table.")
                .inputSchema(inputSchema)
                .build();

        return List.of(new McpServerFeatures.AsyncToolSpecification(
                tool,
                (exchange, params) -> executeGetSchema(params)
        ));
    }

    private Mono<McpSchema.CallToolResult> executeGetSchema(final Map<String, Object> params)
    {
        try
        {
            final var scope = McpToolUtils.resolveScope(params);
            final var container = assets.container();

            return container.schemas(scope)
                    .map(schema -> Map.of(
                            "name", (Object) schema.getName(),
                            "schema", (Object) schema.getSchema()
                    ))
                    .collectList()
                    .map(schemas -> {
                        try
                        {
                            return McpToolUtils.successResult(objectMapper.writeValueAsString(schemas));
                        }
                        catch(Exception e)
                        {
                            return McpToolUtils.errorResult("Error serializing schemas", e);
                        }
                    })
                    .onErrorResume(e -> Mono.just(McpToolUtils.errorResult("Error retrieving schemas", e)))
                    .subscribeOn(Schedulers.boundedElastic());
        }
        catch(Exception e)
        {
            return Mono.just(McpToolUtils.errorResult("Error retrieving schemas", e));
        }
    }
}
