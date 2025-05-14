package org.oagi.score.gateway.http.api.release_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.Collections;
import java.util.List;

@Data
public class ReleaseValidationRequest {

    private ReleaseId releaseId;
    private List<AccManifestId> assignedAccComponentManifestIds = Collections.emptyList();
    private List<AsccpManifestId> assignedAsccpComponentManifestIds = Collections.emptyList();
    private List<BccpManifestId> assignedBccpComponentManifestIds = Collections.emptyList();
    private List<CodeListManifestId> assignedCodeListComponentManifestIds = Collections.emptyList();
    private List<AgencyIdListManifestId> assignedAgencyIdListComponentManifestIds = Collections.emptyList();
    private List<DtManifestId> assignedDtComponentManifestIds = Collections.emptyList();
}
