package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

@Data
public class ResetDetailBIERequest {

    private TopLevelAsbiepId topLevelAsbiepId;
    private String bieType;
    private String path;
}
