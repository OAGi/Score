package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;

public record CopyModuleSetModuleRequest(
        ModuleSetId moduleSetId,
        ModuleId parentModuleId,
        ModuleId targetModuleId,
        boolean copySubModules) {

    // Copy constructor to create a new instance with moduleSetId and parentModuleId
    public CopyModuleSetModuleRequest withModuleSetIdAndParentModuleId(ModuleSetId moduleSetId, ModuleId parentModuleId) {
        return new CopyModuleSetModuleRequest(moduleSetId, parentModuleId, targetModuleId, copySubModules);
    }

}
