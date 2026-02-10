package boozilla.houston.mcp.tool;

import boozilla.houston.container.ManifestContainer;
import boozilla.houston.mcp.McpToolProvider;
import boozilla.houston.mcp.McpToolUtils;
import com.google.protobuf.util.JsonFormat;
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
public class ManifestTool implements McpToolProvider {
    private final ManifestContainer container;

    @Override
    public List<McpServerFeatures.AsyncToolSpecification> tools()
    {
        final var inputSchema = new McpSchema.JsonSchema(
                "object",
                Map.of(
                        "name", Map.of(
                                "type", "string",
                                "description", "The manifest key to retrieve"
                        )
                ),
                List.of("name"),
                false,
                null,
                null
        );

        final var tool = McpSchema.Tool.builder()
                .name("get_manifest")
                .description("Retrieve application manifest metadata, including maintenance information.")
                .inputSchema(inputSchema)
                .build();

        return List.of(new McpServerFeatures.AsyncToolSpecification(
                tool,
                (exchange, params) -> executeGetManifest(params)
        ));
    }

    private Mono<McpSchema.CallToolResult> executeGetManifest(final Map<String, Object> params)
    {
        try
        {
            final var name = McpToolUtils.requireStringParam(params, "name");

            return container.get(name)
                    .map(manifest -> {
                        try
                        {
                            final var json = JsonFormat.printer().print(manifest);
                            return McpToolUtils.successResult(json);
                        }
                        catch(Exception e)
                        {
                            return McpToolUtils.errorResult("Error serializing manifest", e);
                        }
                    })
                    .switchIfEmpty(Mono.just(
                            McpToolUtils.notFoundResult("Manifest '" + name + "' not found")
                    ))
                    .onErrorResume(e -> Mono.just(McpToolUtils.errorResult("Error retrieving manifest", e)))
                    .subscribeOn(Schedulers.boundedElastic());
        }
        catch(Exception e)
        {
            return Mono.just(McpToolUtils.errorResult("Error retrieving manifest", e));
        }
    }
}
