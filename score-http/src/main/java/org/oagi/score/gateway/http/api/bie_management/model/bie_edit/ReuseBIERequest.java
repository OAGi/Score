package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;

@Data
public class ReuseBIERequest {

    private TopLevelAsbiepId topLevelAsbiepId;
    private AsccpManifestId asccpManifestId;
    private AsccManifestId asccManifestId;
    private AccManifestId accManifestId;
    private String asbiePath;
    private String asbieHashPath;
    private String fromAbiePath;
    private String fromAbieHashPath;
    private TopLevelAsbiepId reuseTopLevelAsbiepId;

}
