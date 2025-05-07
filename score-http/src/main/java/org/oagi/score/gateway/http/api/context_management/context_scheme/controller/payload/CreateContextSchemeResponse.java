package org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload;

import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;

public record CreateContextSchemeResponse(ContextSchemeId contextSchemeId, String status, String statusMessage) {
}
