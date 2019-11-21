package org.oagi.srt.gateway.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "org.oagi.srt")
@RestController
public class SrtHttpGatewayApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SrtHttpGatewayApplication.class, args);
    }
}
