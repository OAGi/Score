package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;

@Data
public class TopLevelAsbiepRequest {

    private TopLevelAsbiepId topLevelAsbiepId;
    private String version;
    private String status;
    private Boolean inverseMode;
    private BieForOasDoc bieForOasDoc;

}
