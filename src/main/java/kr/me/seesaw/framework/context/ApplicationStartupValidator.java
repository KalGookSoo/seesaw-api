package kr.me.seesaw.framework.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

import java.util.Arrays;

/**
 * 애플리케이션 시작 시 필수 설정을 검증하는 리스너
 */
public class ApplicationStartupValidator implements ApplicationListener<ApplicationEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            validateProfile((ApplicationEnvironmentPreparedEvent) event);
        }
    }

    /**
     * 프로파일 설정 검증
     */
    private void validateProfile(ApplicationEnvironmentPreparedEvent event) {
        String[] activeProfiles = event.getEnvironment().getActiveProfiles();

        if (activeProfiles.length == 0 || Arrays.asList(activeProfiles).contains("default")) {
            logger.error("애플리케이션 실행을 위해서는 명시적인 프로파일 설정이 필요합니다.");
            logger.error("'default' 프로파일은 허용되지 않습니다.");
            logger.error("예: --spring.profiles.active=local 또는 --spring.profiles.active=dev");
            System.exit(1);
        }
    }

}
