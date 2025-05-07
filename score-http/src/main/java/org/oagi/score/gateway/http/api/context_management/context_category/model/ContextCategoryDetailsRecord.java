package org.oagi.score.gateway.http.api.context_management.context_category.model;

import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

/**
 * Represents the detailed information of a context category.
 * This record contains the context category ID, GUID, meaning, description, usage status,
 * and information about when it was created and last updated.
 */
public record ContextCategoryDetailsRecord(ContextCategoryId contextCategoryId,
                                           Guid guid, String name, String description,
                                           boolean used,
                                           WhoAndWhen created,
                                           WhoAndWhen lastUpdated) {
}
