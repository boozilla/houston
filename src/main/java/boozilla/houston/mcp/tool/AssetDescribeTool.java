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

import java.util.*;

@Component
@ConditionalOnProperty(name = "armeria.mcp.enabled", havingValue = "true")
@AllArgsConstructor
public class AssetDescribeTool implements McpToolProvider {
    private final AssetContainers assets;
    private final ObjectMapper objectMapper;

    @Override
    public List<McpServerFeatures.AsyncToolSpecification> tools()
    {
        final var inputSchema = new McpSchema.JsonSchema(
                "object",
                Map.of(
                        "name", Map.of(
                                "type", "string",
                                "description", "The name of the asset table to describe"
                        ),
                        "scope", McpToolUtils.SCOPE_SCHEMA
                ),
                List.of("name"),
                false,
                null,
                null
        );

        final var tool = McpSchema.Tool.builder()
                .name("describe_asset")
                .description("Get detailed structure information for a specific asset table, including schema, size, and partition info.")
                .inputSchema(inputSchema)
                .build();

        return List.of(new McpServerFeatures.AsyncToolSpecification(
                tool,
                (exchange, params) -> executeDescribe(params)
        ));
    }

    private Mono<McpSchema.CallToolResult> executeDescribe(final Map<String, Object> params)
    {
        try
        {
            final var name = McpToolUtils.requireStringParam(params, "name");
            final var scope = McpToolUtils.resolveScope(params);
            final var container = assets.container();

            return container.list(scope, Set.of(name))
                    .next()
                    .flatMap(sheet -> container.schemas(scope)
                            .filter(s -> s.getName().equals(name))
                            .next()
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .map(schemaOpt -> {
                                try
                                {
                                    final var result = new LinkedHashMap<String, Object>();
                                    result.put("name", sheet.getName());
                                    result.put("size", sheet.getSize());
                                    result.put("commitId", sheet.getCommitId());
                                    result.put("partitions", sheet.getPartitionList());

                                    schemaOpt.ifPresent(schema -> result.put("schema", schema.getSchema()));

                                    return McpToolUtils.successResult(objectMapper.writeValueAsString(result));
                                }
                                catch(Exception e)
                                {
                                    return McpToolUtils.errorResult("Error serializing asset description", e);
                                }
                            })
                    )
                    .switchIfEmpty(Mono.just(
                            McpToolUtils.notFoundResult("Asset table '" + name + "' not found in scope " + scope)
                    ))
                    .onErrorResume(e -> Mono.just(McpToolUtils.errorResult("Error describing asset", e)))
                    .subscribeOn(Schedulers.boundedElastic());
        }
        catch(Exception e)
        {
            return Mono.just(McpToolUtils.errorResult("Error describing asset", e));
        }
    }
}
