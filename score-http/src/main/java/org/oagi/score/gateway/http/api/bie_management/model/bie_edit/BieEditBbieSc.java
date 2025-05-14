package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;

@Data
public class BieEditBbieSc {

    private BbieScId bbieScId;
    private BbieId bbieId;
    private DtScManifestId dtScManifestId;
    private boolean used;

}
