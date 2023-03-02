package org.oagi.score.repo.api.configuration;

import org.oagi.score.repo.api.user.model.ScoreUser;

public interface ConfigurationWriteRepository {

    void updateBooleanConfiguration(ScoreUser user, String configurationName, boolean value);

}
