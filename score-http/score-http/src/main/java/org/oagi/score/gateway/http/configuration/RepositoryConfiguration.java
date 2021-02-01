package org.oagi.score.gateway.http.configuration;

import org.jooq.DSLContext;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.jooq.JooqAccessControlScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {

    @Autowired
    private DSLContext dslContext;

    @Bean
    public ScoreRepositoryFactory scoreRepositoryFactory() {
        return new JooqAccessControlScoreRepositoryFactory(new JooqScoreRepositoryFactory(dslContext));
    }
}
