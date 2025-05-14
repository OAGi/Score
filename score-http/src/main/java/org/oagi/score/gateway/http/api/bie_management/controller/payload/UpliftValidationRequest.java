package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingMapping;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.util.List;

@Data
public class UpliftValidationRequest {
    private ScoreUser requester;
    private TopLevelAsbiepId topLevelAsbiepId;
    private ReleaseId targetReleaseId;
    private List<BieUpliftingMapping> mappingList;
    private AsccpManifestId targetAsccpManifestId;
}
