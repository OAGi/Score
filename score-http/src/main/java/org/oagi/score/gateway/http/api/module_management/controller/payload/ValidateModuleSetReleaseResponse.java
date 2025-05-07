package org.oagi.score.gateway.http.api.module_management.controller.payload;

import java.util.Map;

public record ValidateModuleSetReleaseResponse(Map<String, String> results,
                                               String requestId,
                                               long progress,
                                               long length,
                                               boolean done) {
}
