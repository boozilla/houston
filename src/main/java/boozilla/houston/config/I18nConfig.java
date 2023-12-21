package boozilla.houston.config;

import org.springframework.beans.factory.annotation.Value;
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
        messageSource.setDefaultLocale(Locale.US);
        messageSource.setUseCodeAsDefaultMessage(true);

        return messageSource;
    }

    @Bean
    public MessageSourceAccessor messageSourceAccessor(@Value("spring.messages.locale") final String language)
    {
        final var locale = Locale.of(language);
        return new MessageSourceAccessor(messageSource(), locale);
    }
}
