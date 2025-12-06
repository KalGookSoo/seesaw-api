package kr.me.seesaw;

import kr.me.seesaw.config.ApplicationStartupValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@Slf4j
@SpringBootApplication
public class SeesawApiApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SeesawApiApplication.class);
        app.addListeners(new ApplicationPidFileWriter("seesaw-api.pid"));
        app.addListeners(new ApplicationStartupValidator());
        app.run(args);
    }

}
