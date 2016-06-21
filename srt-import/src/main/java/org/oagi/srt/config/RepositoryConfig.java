package org.oagi.srt.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(
        basePackages = "org.oagi.srt.repository"
)
@EnableJpaRepositories(
        basePackages = "org.oagi.srt.repository"
)
public class RepositoryConfig {
}
