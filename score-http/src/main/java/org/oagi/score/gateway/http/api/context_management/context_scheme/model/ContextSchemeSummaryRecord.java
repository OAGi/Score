package org.oagi.score.gateway.http.api.context_management.context_scheme.model;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategorySummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

/**
 * Represents a summary of a context scheme, including its ID, GUID, associated context category, scheme ID,
 * meaning, agency ID, version ID, and description.
 */
public record ContextSchemeSummaryRecord(ContextSchemeId contextSchemeId,
                                         Guid guid,
                                         ContextCategorySummaryRecord contextCategory,
                                         String schemeId, String schemeName,
                                         String schemeAgencyId, String schemeVersionId,
                                         String description) {
}
