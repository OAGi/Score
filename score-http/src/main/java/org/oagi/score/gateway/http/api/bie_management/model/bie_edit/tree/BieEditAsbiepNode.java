package org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditNode;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAsbiepNode extends BieEditNode {

    private AsbieId asbieId;
    private AsccManifestId asccManifestId;
    private AsbiepId asbiepId;
    private AsccpManifestId asccpManifestId;
    private AbieId abieId;
    private AccManifestId accManifestId;

}
