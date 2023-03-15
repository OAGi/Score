package org.oagi.score.repo.api.impl.jooq.configuration;

import org.jooq.DSLContext;
import org.oagi.score.repo.api.configuration.ConfigurationWriteRepository;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ConfigurationRecord;
import org.oagi.score.repo.api.user.model.ScoreUser;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Configuration.CONFIGURATION;

public class JooqConfigurationWriteRepository
        extends JooqScoreRepository
        implements ConfigurationWriteRepository {

    public JooqConfigurationWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    public void upsertBooleanConfiguration(ScoreUser user, String configurationName, boolean value) {
        ConfigurationRecord configurationRecord = dslContext().selectFrom(CONFIGURATION)
                .where(and(
                        CONFIGURATION.NAME.eq(configurationName),
                        CONFIGURATION.TYPE.eq("Boolean")
                ))
                .fetchOptional().orElse(null);

        if (configurationRecord == null) {
            dslContext().insertInto(CONFIGURATION)
                    .set(CONFIGURATION.NAME, configurationName)
                    .set(CONFIGURATION.VALUE, (value) ? "true" : "false")
                    .set(CONFIGURATION.TYPE, "Boolean")
                    .execute();
        } else {
            dslContext().update(CONFIGURATION)
                    .set(CONFIGURATION.VALUE, (value) ? "true" : "false")
                    .where(CONFIGURATION.CONFIGURATION_ID.eq(configurationRecord.getConfigurationId()))
                    .execute();
        }
    }
}
