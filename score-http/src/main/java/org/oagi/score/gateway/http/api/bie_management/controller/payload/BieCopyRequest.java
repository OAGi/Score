package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;

import java.util.List;

public record BieCopyRequest(List<BusinessContextId> bizCtxIdList) {
}
