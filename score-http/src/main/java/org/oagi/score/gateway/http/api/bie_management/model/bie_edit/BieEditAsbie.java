package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;

@Data
public class BieEditAsbie {

    private AsbieId asbieId;
    private AbieId fromAbieId;
    private AsbiepId toAsbiepId;
    private AsccManifestId basedAsccManifestId;
    private boolean used;

    private int cardinalityMin;
    private int cardinalityMax;

}
