package boozilla.houston.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

@Configuration
public class I18nConfig {
    @Bean
    public Locale getLocale(@Value("${app.locale:en-US}") final String localeCode)
    {
        return Locale.forLanguageTag(localeCode.replace('_', '-'));
    }

    @Bean
    public MessageSource messageSource(final Locale locale)
    {
        final var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(locale);

        return messageSource;
    }

    @Bean
    public MessageSourceAccessor messageSourceAccessor(final MessageSource messageSource,
                                                       final Locale locale)
    {
        return new MessageSourceAccessor(messageSource, locale);
    }
}
