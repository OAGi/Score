package org.oagi.score.gateway.http.api.context_management.context_category.repository.criteria;

import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

/**
 * Criteria for filtering context categories in a list.
 * This record includes fields for filtering by name, description,
 * updater login IDs, and last updated timestamp range.
 */
public record ContextCategoryListFilterCriteria(String name, String description,
                                                Collection<String> updaterLoginIdSet,
                                                DateRangeCriteria lastUpdatedTimestampRange) {
}
