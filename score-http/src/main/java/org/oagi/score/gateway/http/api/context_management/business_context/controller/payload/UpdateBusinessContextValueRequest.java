package org.oagi.score.gateway.http.api.context_management.business_context.controller.payload;

import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextValueId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;

public record UpdateBusinessContextValueRequest(BusinessContextValueId businessContextValueId,
                                                ContextSchemeValueId contextSchemeValueId) {
}
