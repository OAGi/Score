package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;

@Data
public class BieEditBbiep {

    private BbiepId bbiepId;
    private BccpManifestId basedBccpManifestId;

}
