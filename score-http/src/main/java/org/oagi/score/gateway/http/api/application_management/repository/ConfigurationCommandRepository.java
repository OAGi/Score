package org.oagi.score.gateway.http.api.application_management.repository;

public interface ConfigurationCommandRepository {

    void insertBooleanConfigurationIfNotExists(String configurationName, boolean value);

    void upsertBooleanConfiguration(String configurationName, boolean value);

    void insertConfigurationIfNotExists(String configurationName, String value);

    void upsertConfiguration(String configurationName, String value);

}
