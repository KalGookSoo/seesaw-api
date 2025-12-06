package kr.me.seesaw;

import kr.me.seesaw.config.ApplicationStartupValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class SeesawApiApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SeesawApiApplication.class);
        application.addListeners(new ApplicationStartupValidator());
        application.addListeners(new ApplicationPidFileWriter());
        application.run(args);
    }

}
