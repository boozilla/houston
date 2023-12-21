package boozilla.houston.annotation;

import boozilla.houston.decorator.factory.ScopeDecoratorFactory;
import com.linecorp.armeria.server.annotation.DecoratorFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DecoratorFactory(ScopeDecoratorFactory.class)
public @interface ScopeService {
}
