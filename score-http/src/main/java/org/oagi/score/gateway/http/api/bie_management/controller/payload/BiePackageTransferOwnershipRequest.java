package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;

import java.util.Map;

public record BiePackageTransferOwnershipRequest(BiePackageId biePackageId,
                                                 String targetLoginId,
                                                 Boolean sendNotification,
                                                 Map<String, Object> mailParameters) {

    public BiePackageTransferOwnershipRequest withBiePackageId(BiePackageId biePackageId) {
        return new BiePackageTransferOwnershipRequest(biePackageId, this.targetLoginId, this.sendNotification, this.mailParameters);
    }
}
