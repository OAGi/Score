package org.oagi.score.gateway.http.api.cc_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;

import java.util.List;

@Data
public class CcUpdateResponse {

    private List<AccManifestId> accNodeResults;
    private List<AsccManifestId> asccNodeResults;
    private List<AsccpManifestId> asccpNodeResults;
    private List<BccManifestId> bccNodeResults;
    private List<BccpManifestId> bccpNodeResults;
    private List<DtManifestId> dtNodeResults;
    private List<DtScManifestId> dtScNodeResults;

}
