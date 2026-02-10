package boozilla.houston.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;

import java.util.List;

public interface McpResourceProvider {
    List<McpServerFeatures.AsyncResourceSpecification> resources();
}
