package kr.me.seesaw.framework.config;

import kr.me.seesaw.core.support.file.FileManager;
import kr.me.seesaw.core.support.file.LocalFileManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileManagerConfig {

    @Bean
    public FileManager fileManager() {
        return new LocalFileManager();
    }

}
