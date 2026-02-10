package boozilla.houston.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;

import java.util.List;

public interface McpToolProvider {
    List<McpServerFeatures.AsyncToolSpecification> tools();
}
