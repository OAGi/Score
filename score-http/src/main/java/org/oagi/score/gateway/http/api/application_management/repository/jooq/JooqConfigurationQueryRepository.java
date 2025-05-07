package org.oagi.score.gateway.http.api.application_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.application_management.repository.ConfigurationQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.CONFIGURATION;

public class JooqConfigurationQueryRepository extends JooqBaseRepository implements ConfigurationQueryRepository {

    public JooqConfigurationQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public String getConfigurationValueByName(String paramConfigName) {
        return dslContext().select(CONFIGURATION.VALUE)
                .from(CONFIGURATION)
                .where(CONFIGURATION.NAME.eq(paramConfigName))
                .fetchOne(CONFIGURATION.VALUE);
    }

}
