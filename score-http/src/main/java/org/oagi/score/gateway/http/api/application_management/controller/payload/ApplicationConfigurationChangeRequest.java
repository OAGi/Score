package org.oagi.score.gateway.http.api.application_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
public class ApplicationConfigurationChangeRequest {

    private Boolean tenantEnabled;

    private Boolean businessTermEnabled;

    private Boolean bieInverseModeEnabled;

    private Boolean functionsRequiringEmailTransmissionEnabled;

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
