package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.module_management.model.ModuleId;

public record CreateModuleSetModuleResponse(ModuleId moduleId, String status, String statusMessage) {
}
