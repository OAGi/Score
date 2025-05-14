package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;

public record UpdateModuleSetRequest(
        ModuleSetId moduleSetId,
        String name,
        String description) {

    // Copy constructor to create a new instance with a moduleSetId
    public UpdateModuleSetRequest withModuleSetId(ModuleSetId moduleSetId) {
        return new UpdateModuleSetRequest(moduleSetId, name, description);
    }

}
