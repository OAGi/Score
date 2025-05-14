package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;

public record CreateModuleSetReleaseResponse(ModuleSetReleaseId moduleSetReleaseId, String status, String statusMessage) {
}
