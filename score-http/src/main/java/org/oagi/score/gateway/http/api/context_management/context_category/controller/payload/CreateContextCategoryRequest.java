package org.oagi.score.gateway.http.api.context_management.context_category.controller.payload;

public record CreateContextCategoryRequest(
        String name,
        String description) {
}
