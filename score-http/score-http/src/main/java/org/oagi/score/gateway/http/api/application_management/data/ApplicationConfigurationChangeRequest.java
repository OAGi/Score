package org.oagi.score.gateway.http.api.application_management.data;

import lombok.Data;
import org.oagi.score.repo.api.impl.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
public class ApplicationConfigurationChangeRequest {

    private Boolean tenantEnabled;

    private Boolean businessTermEnabled;

    private Boolean bieInverseModeEnabled;

    // General
    private Map<String, String> keyValueMap = new HashMap<>();

    public ApplicationConfigurationChangeRequest withKeyAndValue(String key, String value) {
        if (!StringUtils.hasLength(key)) {
            return this;
        }
        keyValueMap.put(key, StringUtils.hasLength(value) ? value : null);
        return this;
    }

}
