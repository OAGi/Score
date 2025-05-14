package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BieId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;

@Data
public class BieEditUsed {

    private boolean used;
    private String hashPath;
    private BieId bieId;
    private ManifestId manifestId;
    private String type;
    private TopLevelAsbiepId ownerTopLevelAsbiepId;
    private String displayName;
    private int cardinalityMin;
    private int cardinalityMax;
    private boolean deprecated;

}
