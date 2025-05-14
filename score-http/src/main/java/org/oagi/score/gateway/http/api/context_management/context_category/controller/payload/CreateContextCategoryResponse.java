package org.oagi.score.gateway.http.api.context_management.context_category.controller.payload;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;

public record CreateContextCategoryResponse(ContextCategoryId contextCategoryId, String status, String statusMessage) {
}
