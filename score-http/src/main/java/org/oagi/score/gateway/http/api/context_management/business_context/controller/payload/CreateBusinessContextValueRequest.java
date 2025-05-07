package org.oagi.score.gateway.http.api.context_management.business_context.controller.payload;

import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;

public record CreateBusinessContextValueRequest(ContextSchemeValueId contextSchemeValueId) {
}
