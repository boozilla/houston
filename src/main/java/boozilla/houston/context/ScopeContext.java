package boozilla.houston.context;

import boozilla.houston.HoustonHeaders;
import boozilla.houston.asset.Scope;
import com.linecorp.armeria.server.ServiceRequestContext;
import io.netty.util.AttributeKey;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class ScopeContext {
    public final AttributeKey<Scope> ATTR_SCOPE_KEY = AttributeKey.newInstance(HoustonHeaders.SCOPE);

    // Request context 안에서만 호출돼야 한다.
    public Scope get()
    {
        return Objects.requireNonNull(ServiceRequestContext.current().attr(ATTR_SCOPE_KEY));
    }
}
