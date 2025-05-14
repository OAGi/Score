package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import java.util.List;

@Data
public class InitUpliftBiePackageResponse {

    private BiePackageId upliftedBiePackageId;

    private List<TopLevelAsbiepId> sourceTopLevelAsbiepIdList;

    public InitUpliftBiePackageResponse(BiePackageId upliftedBiePackageId, List<TopLevelAsbiepId> sourceTopLevelAsbiepIdList) {
        this.upliftedBiePackageId = upliftedBiePackageId;
        this.sourceTopLevelAsbiepIdList = sourceTopLevelAsbiepIdList;
    }
}
