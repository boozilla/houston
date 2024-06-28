package boozilla.houston.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

@Configuration
public class I18nConfig {
    private ReloadableResourceBundleMessageSource messageSource()
    {
        final var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.getDefault());
        messageSource.setUseCodeAsDefaultMessage(true);

        return messageSource;
    }

    @Bean
    public MessageSourceAccessor messageSourceAccessor()
    {
        return new MessageSourceAccessor(messageSource(), Locale.getDefault());
    }
}
