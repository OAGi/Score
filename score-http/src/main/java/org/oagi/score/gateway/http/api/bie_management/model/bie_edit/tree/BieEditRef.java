package org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;

@Data
@EqualsAndHashCode
public class BieEditRef {

    private AsbieId asbieId;
    private AsccManifestId basedAsccManifestId;
    private String hashPath;
    private TopLevelAsbiepId topLevelAsbiepId;
    private TopLevelAsbiepId basedTopLevelAsbiepId;
    private TopLevelAsbiepId refTopLevelAsbiepId;
    private TopLevelAsbiepId refBasedTopLevelAsbiepId;
    private boolean refInverseMode;

}
