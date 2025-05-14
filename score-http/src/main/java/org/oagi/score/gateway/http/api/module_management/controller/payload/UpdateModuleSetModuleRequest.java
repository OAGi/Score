package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public record UpdateModuleSetModuleRequest(
        ModuleId moduleId,
        ModuleSetId moduleSetId,
        NamespaceId namespaceId,
        String name,
        String versionNum) {

    // Copy constructor to create a new instance with moduleId and moduleSetId
    public UpdateModuleSetModuleRequest withModuleIdAndModuleSetId(ModuleId moduleId, ModuleSetId moduleSetId) {
        return new UpdateModuleSetModuleRequest(moduleId, moduleSetId, namespaceId, name, versionNum);
    }

}
