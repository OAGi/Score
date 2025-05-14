package org.oagi.score.gateway.http.api.context_management.business_context.controller.payload;

import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;

import java.util.Collection;

public record DiscardBusinessContextRequest(
        Collection<BusinessContextId> businessContextIdList) {

}
