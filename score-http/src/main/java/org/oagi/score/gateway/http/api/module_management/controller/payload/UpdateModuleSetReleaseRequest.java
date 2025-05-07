package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;

public record UpdateModuleSetReleaseRequest(
        ModuleSetReleaseId moduleSetReleaseId,
        String name,
        String description,
        boolean isDefault) {

    // Copy constructor to create a new instance with a moduleSetReleaseId
    public UpdateModuleSetReleaseRequest withModuleSetReleaseId(ModuleSetReleaseId moduleSetReleaseId) {
        return new UpdateModuleSetReleaseRequest(moduleSetReleaseId, name, description, isDefault);
    }

}
