package org.oagi.score.gateway.http.api.application_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.application_management.repository.ConfigurationCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.ConfigurationRecord;
import org.oagi.score.gateway.http.common.util.StringUtils;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Configuration.CONFIGURATION;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

public class JooqConfigurationCommandRepository extends JooqBaseRepository implements ConfigurationCommandRepository {

    public JooqConfigurationCommandRepository(DSLContext dslContext,
                                              ScoreUser requester,
                                              RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public void insertBooleanConfigurationIfNotExists(String configurationName, boolean value) {
        insertConfiguration(configurationName, "Boolean", (value) ? "true" : "false", false);
    }

    @Override
    public void upsertBooleanConfiguration(String configurationName, boolean value) {
        insertConfiguration(configurationName, "Boolean", (value) ? "true" : "false", true);
    }

    @Override
    public void insertConfigurationIfNotExists(String configurationName, String value) {
        insertConfiguration(configurationName, "String", value, false);
    }

    @Override
    public void upsertConfiguration(String configurationName, String value) {
        insertConfiguration(configurationName, "String", value, true);
    }

    public void insertConfiguration(String configurationName, String type, String value, boolean overwrite) {
        ConfigurationRecord configurationRecord = dslContext().selectFrom(CONFIGURATION)
                .where(and(
                        CONFIGURATION.NAME.eq(configurationName),
                        CONFIGURATION.TYPE.eq(type)
                ))
                .fetchOptional().orElse(null);

        if (configurationRecord == null) {
            dslContext().insertInto(CONFIGURATION)
                    .set(CONFIGURATION.NAME, configurationName)
                    .set(CONFIGURATION.VALUE, value)
                    .set(CONFIGURATION.TYPE, type)
                    .execute();
        }

        String prevValue = configurationRecord.getValue();
        if (StringUtils.equals(prevValue, value)) {
            return;
        }
        if (!hasLength(value) || overwrite) {
            dslContext().update(CONFIGURATION)
                    .set(CONFIGURATION.VALUE, value)
                    .where(CONFIGURATION.CONFIGURATION_ID.eq(configurationRecord.getConfigurationId()))
                    .execute();
        }
    }

}
