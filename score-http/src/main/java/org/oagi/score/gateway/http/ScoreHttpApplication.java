package org.oagi.score.gateway.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "org.oagi.score")
@RestController
public class ScoreHttpApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ScoreHttpApplication.class, args);
    }
}
