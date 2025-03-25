package boozilla.houston.annotation;

import boozilla.houston.decorator.auth.HttpAuthorizer;
import boozilla.houston.decorator.factory.SecureDecoratorFactory;
import com.linecorp.armeria.server.annotation.DecoratorFactory;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Service
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DecoratorFactory(SecureDecoratorFactory.class)
public @interface SecuredService {
    Class<? extends HttpAuthorizer>[] value() default HttpAuthorizer.class;
}
