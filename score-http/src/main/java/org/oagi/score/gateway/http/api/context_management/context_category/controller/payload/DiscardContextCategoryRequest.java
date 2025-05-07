package org.oagi.score.gateway.http.api.context_management.context_category.controller.payload;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;

import java.util.Collection;

public record DiscardContextCategoryRequest(
        Collection<ContextCategoryId> contextCategoryIdList) {

}
