package kr.me.seesaw.config;

import kr.me.seesaw.message.CmsMessageSource;
import org.apache.commons.codec.CharEncoding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class I18nConfig {
    private final String cacheSeconds;

    public I18nConfig(@Value("${spring.messages.cache-duration}") String cacheSeconds) {
        this.cacheSeconds = cacheSeconds;
    }

    @Bean
    public MessageSource messageSource() {
        CmsMessageSource messageSource = new CmsMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding(CharEncoding.UTF_8);
        messageSource.setCacheSeconds(Integer.parseInt(cacheSeconds));
        return messageSource;
    }
}
