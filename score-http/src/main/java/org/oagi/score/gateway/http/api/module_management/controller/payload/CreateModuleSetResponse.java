package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;

public record CreateModuleSetResponse(ModuleSetId moduleSetId, String status, String statusMessage) {
}
