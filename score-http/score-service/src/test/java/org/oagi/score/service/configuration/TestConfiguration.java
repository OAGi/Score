package org.oagi.score.service.configuration;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.jooq.JooqAccessControlScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepositoryFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "org.oagi.score.service")
public class TestConfiguration {

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mysql://127.0.0.1:3306/oagi");
        dataSourceBuilder.username("oagi");
        dataSourceBuilder.password("oagi");
        return dataSourceBuilder.build();
    }

    @Bean
    public DSLContext dslContext() {
        return new DefaultDSLContext(dataSource(), SQLDialect.MYSQL);
    }

    @Bean
    public ScoreRepositoryFactory scoreRepositoryFactory() {
        return new JooqAccessControlScoreRepositoryFactory(new JooqScoreRepositoryFactory(dslContext()));
    }

}
