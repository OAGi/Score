package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageId;

public record UpdateBiePackageRequest(
        BiePackageId biePackageId,
        String name,
        String versionId,
        String versionName,
        String description,
        String revisionReason,
        BieState state) {

    public UpdateBiePackageRequest withBiePackageId(BiePackageId biePackageId) {
        return new UpdateBiePackageRequest(biePackageId, name, versionId, versionName, description, revisionReason, state);
    }

}
