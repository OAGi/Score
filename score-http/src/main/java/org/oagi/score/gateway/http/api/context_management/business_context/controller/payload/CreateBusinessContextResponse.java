package org.oagi.score.gateway.http.api.context_management.business_context.controller.payload;

import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;

public record CreateBusinessContextResponse(BusinessContextId businessContextId, String status, String statusMessage) {
}
