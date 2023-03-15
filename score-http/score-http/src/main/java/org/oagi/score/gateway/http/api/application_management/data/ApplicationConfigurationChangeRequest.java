package org.oagi.score.gateway.http.api.application_management.data;

import lombok.Data;

@Data
public class ApplicationConfigurationChangeRequest {

    private Boolean tenantEnabled;

    private Boolean businessTermEnabled;

    private Boolean bieInverseModeEnabled;

}
