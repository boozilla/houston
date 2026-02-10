package boozilla.houston.mcp.tool;

import boozilla.houston.asset.AssetContainers;
import boozilla.houston.asset.AssetData;
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
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "armeria.mcp.enabled", havingValue = "true")
@AllArgsConstructor
public class AssetQueryTool implements McpToolProvider {
    private static final Pattern LIMIT_PATTERN = Pattern.compile("(?i)\\bLIMIT\\s+\\d+");

    private final AssetContainers assets;
    private final ObjectMapper objectMapper;

    @Override
    public List<McpServerFeatures.AsyncToolSpecification> tools()
    {
        final var inputSchema = new McpSchema.JsonSchema(
                "object",
                Map.of(
                        "sql", Map.of(
                                "type", "string",
                                "description", "SQL SELECT query to execute against asset data tables"
                        ),
                        "scope", McpToolUtils.SCOPE_SCHEMA,
                        "limit", Map.of(
                                "type", "integer",
                                "description", "Maximum number of rows to return",
                                "default", 100
                        )
                ),
                List.of("sql"),
                false,
                null,
                null
        );

        final var tool = McpSchema.Tool.builder()
                .name("query_assets")
                .description("Execute SQL-like queries against asset data tables. Use standard SELECT syntax.")
                .inputSchema(inputSchema)
                .build();

        return List.of(new McpServerFeatures.AsyncToolSpecification(
                tool,
                (exchange, params) -> executeQuery(params)
        ));
    }

    private Mono<McpSchema.CallToolResult> executeQuery(final Map<String, Object> params)
    {
        try
        {
            final var scope = McpToolUtils.resolveScope(params);
            var sql = McpToolUtils.requireStringParam(params, "sql");
            final var limit = McpToolUtils.getIntParam(params, "limit", 100);

            if(!LIMIT_PATTERN.matcher(sql).find())
            {
                sql = sql.stripTrailing();
                if(sql.endsWith(";"))
                {
                    sql = sql.substring(0, sql.length() - 1);
                }
                sql = sql + " LIMIT " + limit;
            }

            final var container = assets.container();

            return container.query(scope, sql, resultInfo -> {})
                    .map(AssetData::toJsonString)
                    .collectList()
                    .map(results -> {
                        try
                        {
                            return McpToolUtils.successResult(objectMapper.writeValueAsString(results));
                        }
                        catch(Exception e)
                        {
                            return McpToolUtils.errorResult("Error serializing query results", e);
                        }
                    })
                    .onErrorResume(e -> Mono.just(McpToolUtils.errorResult("Error executing query", e)))
                    .subscribeOn(Schedulers.boundedElastic());
        }
        catch(Exception e)
        {
            return Mono.just(McpToolUtils.errorResult("Error executing query", e));
        }
    }
}
