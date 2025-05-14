package org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload;

import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;

public record UpdateContextSchemeValueRequest(ContextSchemeValueId contextSchemeValueId, String value, String meaning) {
}
