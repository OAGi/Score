package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

@Data
public class DeprecateBIERequest {

    private TopLevelAsbiepId topLevelAsbiepId;
    private String reason;
    private String remark;

}
