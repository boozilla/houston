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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "armeria.mcp.enabled", havingValue = "true")
@AllArgsConstructor
public class AssetListTool implements McpToolProvider {
    private final AssetContainers assets;
    private final ObjectMapper objectMapper;

    @Override
    public List<McpServerFeatures.AsyncToolSpecification> tools()
    {
        final var inputSchema = new McpSchema.JsonSchema(
                "object",
                Map.of(
                        "scope", McpToolUtils.SCOPE_SCHEMA,
                        "include", Map.of(
                                "type", "array",
                                "description", "Filter to specific table names. If omitted, all tables are returned.",
                                "items", Map.of("type", "string")
                        )
                ),
                List.of(),
                false,
                null,
                null
        );

        final var tool = McpSchema.Tool.builder()
                .name("list_assets")
                .description("List available asset data tables with their metadata (name, size, commit ID, partitions).")
                .inputSchema(inputSchema)
                .build();

        return List.of(new McpServerFeatures.AsyncToolSpecification(
                tool,
                (exchange, params) -> executeList(params)
        ));
    }

    private Mono<McpSchema.CallToolResult> executeList(final Map<String, Object> params)
    {
        try
        {
            final var scope = McpToolUtils.resolveScope(params);

            final Set<String> includeSet;
            if(params.containsKey("include") && params.get("include") instanceof List<?> includeList)
            {
                includeSet = new HashSet<>();
                for(final var item : includeList)
                {
                    includeSet.add(item.toString());
                }
            }
            else
            {
                includeSet = Set.of();
            }

            final var container = assets.container();

            return container.list(scope, includeSet)
                    .map(sheet -> Map.<String, Object>of(
                            "name", sheet.getName(),
                            "size", sheet.getSize(),
                            "commitId", sheet.getCommitId(),
                            "partitions", sheet.getPartitionList()
                    ))
                    .collectList()
                    .map(sheets -> {
                        try
                        {
                            return McpToolUtils.successResult(objectMapper.writeValueAsString(sheets));
                        }
                        catch(Exception e)
                        {
                            return McpToolUtils.errorResult("Error serializing asset list", e);
                        }
                    })
                    .onErrorResume(e -> Mono.just(McpToolUtils.errorResult("Error listing assets", e)))
                    .subscribeOn(Schedulers.boundedElastic());
        }
        catch(Exception e)
        {
            return Mono.just(McpToolUtils.errorResult("Error listing assets", e));
        }
    }
}
