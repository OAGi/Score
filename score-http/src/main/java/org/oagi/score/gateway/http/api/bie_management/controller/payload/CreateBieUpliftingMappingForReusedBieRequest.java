package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

@Data
public class CreateBieUpliftingMappingForReusedBieRequest {

    private ScoreUser requester;

    private TopLevelAsbiepId topLevelAsbiepId;

    private ReleaseId targetReleaseId;

}
