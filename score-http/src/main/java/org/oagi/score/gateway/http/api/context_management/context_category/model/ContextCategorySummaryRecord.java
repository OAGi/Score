package org.oagi.score.gateway.http.api.context_management.context_category.model;

import org.oagi.score.gateway.http.common.model.Guid;

/**
 * Represents a summary of a context category, including the category ID, GUID, name, and description.
 */
public record ContextCategorySummaryRecord(ContextCategoryId contextCategoryId,
                                           Guid guid,
                                           String name,
                                           String description) {
}
