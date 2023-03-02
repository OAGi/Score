package org.oagi.score.repo.component.app.configuration;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.CONFIGURATION;

;

@Repository
public class ConfigurationRepository {

    @Autowired
    private DSLContext dslContext;

    public String getConfigurationValueByName(String paramConfigName) {
        return dslContext.select(CONFIGURATION.VALUE)
                .from(CONFIGURATION)
                .where(CONFIGURATION.NAME.eq(paramConfigName))
                .fetchOne(CONFIGURATION.VALUE);
    }

}
