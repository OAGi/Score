package org.oagi.score.gateway.http.api.context_management.context_category.controller.payload;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;

public record UpdateContextCategoryRequest(
        ContextCategoryId contextCategoryId,
        String name,
        String description) {

    // Copy constructor to create a new instance with a contextCategoryId
    public UpdateContextCategoryRequest withContextCategoryId(ContextCategoryId contextCategoryId) {
        return new UpdateContextCategoryRequest(contextCategoryId, name, description);
    }

}
