package org.oagi.srt.repository.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages="org.oagi.srt.repository")
public class RepositoryConfig {
}
