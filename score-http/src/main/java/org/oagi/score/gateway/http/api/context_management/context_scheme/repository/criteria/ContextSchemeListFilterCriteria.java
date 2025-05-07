package org.oagi.score.gateway.http.api.context_management.context_scheme.repository.criteria;

import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

/**
 * Criteria for filtering context schemes in a list.
 * This record includes fields for filtering by meaning, description,
 * updater login IDs, and last updated timestamp range.
 */
public record ContextSchemeListFilterCriteria(String name, String description,
                                              Collection<String> updaterLoginIdSet,
                                              DateRangeCriteria lastUpdatedTimestampRange) {
}
