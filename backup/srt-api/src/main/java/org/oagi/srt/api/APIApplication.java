package org.oagi.srt.api;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.oagi.srt"})
public class APIApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(APIApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }

}
