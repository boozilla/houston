package boozilla.houston.mcp.resource;

import boozilla.houston.asset.AssetContainers;
import boozilla.houston.asset.Scope;
import boozilla.houston.mcp.McpResourceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@ConditionalOnProperty(name = "armeria.mcp.enabled", havingValue = "true")
@AllArgsConstructor
public class AssetCatalogResource implements McpResourceProvider {
    private static final String RESOURCE_URI = "houston://assets/catalog";

    private final AssetContainers assets;
    private final ObjectMapper objectMapper;

    @Override
    public List<McpServerFeatures.AsyncResourceSpecification> resources()
    {
        final var resource = new McpSchema.Resource(
                RESOURCE_URI,
                "Asset Catalog",
                "Asset Catalog",
                "List of all available asset sheets with name, size, and commit ID",
                "application/json",
                null,
                null
        );

        return List.of(new McpServerFeatures.AsyncResourceSpecification(
                resource,
                (exchange, request) -> {
                    final var container = assets.container();

                    return container.list(Scope.CLIENT, Set.of())
                            .map(sheet -> Map.of(
                                    "name", (Object) sheet.getName(),
                                    "size", (Object) sheet.getSize(),
                                    "commitId", (Object) sheet.getCommitId(),
                                    "partitions", (Object) sheet.getPartitionList()
                            ))
                            .collectList()
                            .map(sheets -> {
                                try
                                {
                                    final var json = objectMapper.writeValueAsString(sheets);
                                    return new McpSchema.ReadResourceResult(List.of(
                                            new McpSchema.TextResourceContents(
                                                    RESOURCE_URI,
                                                    "application/json",
                                                    json
                                            )
                                    ));
                                }
                                catch(Exception e)
                                {
                                    log.error("Error serializing asset catalog", e);
                                    return new McpSchema.ReadResourceResult(List.of(
                                            new McpSchema.TextResourceContents(
                                                    RESOURCE_URI,
                                                    "text/plain",
                                                    "Error serializing asset catalog: " + e.getMessage()
                                            )
                                    ));
                                }
                            })
                            .onErrorResume(e -> {
                                log.error("Error reading asset catalog", e);
                                return Mono.just(new McpSchema.ReadResourceResult(List.of(
                                        new McpSchema.TextResourceContents(
                                                RESOURCE_URI,
                                                "text/plain",
                                                "Error reading asset catalog: " + e.getMessage()
                                        )
                                )));
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                }
        ));
    }
}
