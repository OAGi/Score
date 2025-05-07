package org.oagi.score.gateway.http.api.application_management.repository;

public interface ConfigurationQueryRepository {

    String getConfigurationValueByName(String paramConfigName);

}
