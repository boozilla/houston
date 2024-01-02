package boozilla.houston.decorator.auth;

import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.server.auth.Authorizer;

public interface HttpAuthorizer extends Authorizer<HttpRequest> {
}
