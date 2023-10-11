package org.oagi.score.gateway.http.api.application_management.data;

import lombok.Data;
import org.oagi.score.repo.api.impl.utils.StringUtils;

@Data
public class ApplicationConfigurationChangeRequest {

    private Boolean tenantEnabled;

    private Boolean businessTermEnabled;

    private Boolean bieInverseModeEnabled;

    // General
    private String key;
    private String value;

    public ApplicationConfigurationChangeRequest withKeyAndValue(String key, String value) {
        this.setKey(StringUtils.hasLength(key) ? key : null);
        this.setValue(StringUtils.hasLength(value) ? value : null);
        return this;
    }

}
