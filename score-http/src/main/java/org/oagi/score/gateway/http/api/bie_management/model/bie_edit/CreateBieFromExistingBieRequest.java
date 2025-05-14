package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;

@Data
public class CreateBieFromExistingBieRequest {

    private String asbieHashPath;
    private TopLevelAsbiepId topLevelAsbiepId;
    private AsccpManifestId asccpManifestId;
}