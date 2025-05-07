package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingMapping;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.util.Collections;
import java.util.List;

@Data
public class UpliftBieRequest {

    private ScoreUser requester;

    private TopLevelAsbiepId topLevelAsbiepId;

    private AsccpManifestId targetAsccpManifestId;

    // If `targetAsccpManifestId` does not exist, the logic will use `targetReleaseId` to find it.
    private ReleaseId targetReleaseId;

    private List<BieUpliftingMapping> customMappingTable = Collections.emptyList();

}
