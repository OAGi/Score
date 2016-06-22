package org.oagi.srt.persistence.populate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan("org.oagi.srt")
@EnableJpaRepositories("org.oagi.srt.repository")
@EntityScan("org.oagi.srt.repository.entity")
@SpringBootApplication
public class ImportApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImportApplication.class);
    }
}
