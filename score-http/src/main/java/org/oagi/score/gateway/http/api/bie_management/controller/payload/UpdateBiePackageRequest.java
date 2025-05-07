package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;

public record UpdateBiePackageRequest(
        BiePackageId biePackageId,
        String versionId,
        String versionName,
        String description,
        BieState state) {

    public UpdateBiePackageRequest withBiePackageId(BiePackageId biePackageId) {
        return new UpdateBiePackageRequest(biePackageId, versionId, versionName, description, state);
    }

}
