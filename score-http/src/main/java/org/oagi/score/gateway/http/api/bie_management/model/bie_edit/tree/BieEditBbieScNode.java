package org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditNode;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBbieScNode extends BieEditNode {

    private BbieScId bbieScId;
    private DtScManifestId dtScManifestId;

}
