package org.oagi.score.repo.api.configuration;

import org.oagi.score.repo.api.user.model.ScoreUser;

public interface ConfigurationWriteRepository {

    void upsertBooleanConfiguration(ScoreUser user, String configurationName, boolean value);

    void upsertConfiguration(ScoreUser user, String configurationName, String value);

}
