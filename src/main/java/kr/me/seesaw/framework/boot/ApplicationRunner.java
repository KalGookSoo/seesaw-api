package kr.me.seesaw.framework.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
public class ApplicationRunner implements CommandLineRunner, ApplicationListener<ApplicationStartedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        logger.info("\n\nApplication started\n\n");
    }

    @Override
    public void run(String... args) {

    }

}
