package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public record CreateModuleSetModuleRequest(
        ModuleSetId moduleSetId,
        ModuleId parentModuleId,
        NamespaceId namespaceId,
        boolean directory,
        String name,
        String versionNum) {

    // Copy constructor to create a new instance with a moduleSetId
    public CreateModuleSetModuleRequest withModuleSetId(ModuleSetId moduleSetId) {
        return new CreateModuleSetModuleRequest(moduleSetId, parentModuleId, namespaceId, directory, name, versionNum);
    }

}
