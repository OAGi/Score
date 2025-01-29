package org.oagi.score.repo.api.configuration;

import org.oagi.score.repo.api.user.model.ScoreUser;

public interface ConfigurationWriteRepository {

    void insertBooleanConfigurationIfNotExists(ScoreUser user, String configurationName, boolean value);

    void upsertBooleanConfiguration(ScoreUser user, String configurationName, boolean value);

    void insertConfigurationIfNotExists(ScoreUser user, String configurationName, String value);

    void upsertConfiguration(ScoreUser user, String configurationName, String value);

}
