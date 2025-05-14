package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.List;

public record AssignCCToModuleRequest(
        List<AssignCCToModuleNode> nodes,
        ModuleId moduleId,
        ModuleSetId moduleSetId,
        ModuleSetReleaseId moduleSetReleaseId,
        ReleaseId releaseId) {

    public AssignCCToModuleRequest withModuleSetReleaseId(ModuleSetReleaseId moduleSetReleaseId) {
        return new AssignCCToModuleRequest(nodes, moduleId, moduleSetId, moduleSetReleaseId, releaseId);
    }
}
