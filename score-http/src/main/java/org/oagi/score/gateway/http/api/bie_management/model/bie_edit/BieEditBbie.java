package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;

@Data
public class BieEditBbie {

    private BbieId bbieId;
    private AbieId fromAbieId;
    private BbiepId toBbiepId;
    private BccManifestId basedBccManifestId;
    private boolean used;

    private int cardinalityMin;
    private int cardinalityMax;

}
