package org.oagi.srt.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = {"org.oagi.srt.repository"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.oagi.srt.repository")
@EntityScan("org.oagi.srt.repository.entity")
public class RepositoryConfiguration {

}
