package org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditNode;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBbiepNode extends BieEditNode {

    private BbieId bbieId;
    private BccManifestId bccManifestId;
    private BbiepId bbiepId;
    private BccpManifestId bccpManifestId;
    private DtManifestId bdtManifestId;
    private boolean attribute;

}
