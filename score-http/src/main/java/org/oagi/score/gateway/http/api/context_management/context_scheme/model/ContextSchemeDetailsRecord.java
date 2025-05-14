package org.oagi.score.gateway.http.api.context_management.context_scheme.model;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategorySummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

/**
 * Represents the details of a context scheme, including its ID, GUID, associated context category,
 * associated code list, scheme ID, name, agency ID, version ID, description, and usage status.
 * Additionally, includes information about when the context scheme was created and last updated.
 */
public record ContextSchemeDetailsRecord(ContextSchemeId contextSchemeId,
                                         Guid guid,
                                         ContextCategorySummaryRecord contextCategory,
                                         String schemeId, String schemeName,
                                         String schemeAgencyId, String schemeVersionId,
                                         String description, boolean used,
                                         WhoAndWhen created,
                                         WhoAndWhen lastUpdated) {
}
