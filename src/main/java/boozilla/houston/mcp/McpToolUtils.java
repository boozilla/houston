package boozilla.houston.mcp;

import boozilla.houston.asset.Scope;
import boozilla.houston.context.ScopeContext;
import com.linecorp.armeria.server.ServiceRequestContext;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@UtilityClass
public class McpToolUtils {
    public final Map<String, Object> SCOPE_SCHEMA = Map.of(
            "type", "string",
            "description", "Data scope to query",
            "enum", List.of("CLIENT", "SERVER"),
            "default", "CLIENT"
    );

    public String requireStringParam(final Map<String, Object> params, final String key)
    {
        final var value = params.get(key);
        if(value == null)
        {
            throw new IllegalArgumentException("Required parameter '" + key + "' is missing");
        }

        return value instanceof String s ? s : String.valueOf(value);
    }

    public String getStringParam(final Map<String, Object> params, final String key, final String defaultValue)
    {
        final var value = params.getOrDefault(key, defaultValue);
        return value instanceof String s ? s : String.valueOf(value);
    }

    public int getIntParam(final Map<String, Object> params, final String key, final int defaultValue)
    {
        final var value = params.get(key);
        if(value == null)
        {
            return defaultValue;
        }

        if(value instanceof Number n)
        {
            return n.intValue();
        }

        try
        {
            return Integer.parseInt(String.valueOf(value));
        }
        catch(NumberFormatException e)
        {
            return defaultValue;
        }
    }

    /**
     * Request context 의 인증 결과에 따라 scope 를 결정한다.
     * - 인증 성공: 요청된 scope 허용 (SERVER 포함)
     * - 인증 실패: CLIENT 로 제한
     * - context 없음: CLIENT 로 제한
     */
    public Scope resolveScope(final Map<String, Object> params)
    {
        final var ctx = ServiceRequestContext.currentOrNull();
        final var maxScope = (ctx != null) ? ctx.attr(ScopeContext.ATTR_SCOPE_KEY) : null;

        final var requestedScopeStr = getStringParam(params, "scope", "CLIENT");

        final Scope requestedScope;
        try
        {
            requestedScope = Scope.valueOf(requestedScopeStr.toUpperCase());
        }
        catch(IllegalArgumentException e)
        {
            return Scope.CLIENT;
        }

        if(maxScope == null)
        {
            return Scope.CLIENT;
        }

        if(maxScope == Scope.CLIENT && requestedScope == Scope.SERVER)
        {
            return Scope.CLIENT;
        }

        return requestedScope;
    }

    public McpSchema.CallToolResult errorResult(final String prefix, final Throwable e)
    {
        log.error("{}: {}", prefix, e.getMessage(), e);
        return McpSchema.CallToolResult.builder()
                .content(List.of(new McpSchema.TextContent(prefix)))
                .isError(true)
                .build();
    }

    public McpSchema.CallToolResult notFoundResult(final String message)
    {
        return McpSchema.CallToolResult.builder()
                .content(List.of(new McpSchema.TextContent(message)))
                .isError(true)
                .build();
    }

    public McpSchema.CallToolResult successResult(final String json)
    {
        return McpSchema.CallToolResult.builder()
                .content(List.of(new McpSchema.TextContent(json)))
                .isError(false)
                .build();
    }
}
