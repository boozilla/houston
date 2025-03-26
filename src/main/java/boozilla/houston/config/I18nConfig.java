package boozilla.houston.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class I18nConfig {
    @Bean
    public ReloadableResourceBundleMessageSource messageSource()
    {
        final var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);

        return messageSource;
    }

    @Bean
    public MessageSourceAccessor messageSourceAccessor(final MessageSource messageSource)
    {
        return new MessageSourceAccessor(messageSource);
    }
}
