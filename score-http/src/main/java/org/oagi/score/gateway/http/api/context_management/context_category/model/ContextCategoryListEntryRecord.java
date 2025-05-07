package org.oagi.score.gateway.http.api.context_management.context_category.model;

import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

/**
 * Represents a list entry for a context category, including the category ID, GUID, meaning, description,
 * status (whether it's in use), and the creation and last update information.
 */
public record ContextCategoryListEntryRecord(ContextCategoryId contextCategoryId,
                                             Guid guid, String name, String description,
                                             boolean used,
                                             WhoAndWhen created,
                                             WhoAndWhen lastUpdated) {
}
