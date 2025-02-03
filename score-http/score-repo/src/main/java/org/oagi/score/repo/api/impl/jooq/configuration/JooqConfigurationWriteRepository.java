package org.oagi.score.repo.api.impl.jooq.configuration;

import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.oagi.score.repo.api.configuration.ConfigurationWriteRepository;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ConfigurationRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.user.model.ScoreUser;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Configuration.CONFIGURATION;
import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

public class JooqConfigurationWriteRepository
        extends JooqScoreRepository
        implements ConfigurationWriteRepository {

    public JooqConfigurationWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    public void insertBooleanConfigurationIfNotExists(ScoreUser user, String configurationName, boolean value) {
        insertConfiguration(user, configurationName, "Boolean", (value) ? "true" : "false", false);
    }

    @Override
    public void upsertBooleanConfiguration(ScoreUser user, String configurationName, boolean value) {
        insertConfiguration(user, configurationName, "Boolean", (value) ? "true" : "false", true);
    }

    @Override
    public void insertConfigurationIfNotExists(ScoreUser user, String configurationName, String value) {
        insertConfiguration(user, configurationName, "String", value, false);
    }

    @Override
    public void upsertConfiguration(ScoreUser user, String configurationName, String value) {
        insertConfiguration(user, configurationName, "String", value, true);
    }

    public void insertConfiguration(ScoreUser user, String configurationName, String type, String value, boolean overwrite) {
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
