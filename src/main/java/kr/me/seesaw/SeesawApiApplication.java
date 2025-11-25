package kr.me.seesaw;

import kr.me.seesaw.config.ApplicationStartupValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SeesawApiApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SeesawApiApplication.class);
        app.addListeners(new ApplicationStartupValidator());
        app.run(args);
    }

}
