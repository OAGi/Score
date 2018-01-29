package org.oagi.srt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan("org.oagi.srt")
@EnableJpaRepositories("org.oagi.srt.repository")
@EntityScan("org.oagi.srt.repository.entity")
@SpringBootApplication(exclude = {
        WebMvcAutoConfiguration.class
})
public class ImportApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImportApplication.class, args);
    }
}
