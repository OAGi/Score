package org.oagi.score.gateway.http.api.context_management.context_scheme.model;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategorySummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

/**
 * Represents a list entry for a context scheme, including the context scheme ID, GUID, associated context category,
 * associated code list, scheme ID, name, agency ID, version ID, description, usage status,
 * and information about when the context scheme was created and last updated.
 */
public record ContextSchemeListEntryRecord(ContextSchemeId contextSchemeId,
                                           Guid guid,
                                           ContextCategorySummaryRecord contextCategory,
                                           String schemeId, String schemeName,
                                           String schemeAgencyId, String schemeVersionId,
                                           String description, boolean used,
                                           WhoAndWhen created,
                                           WhoAndWhen lastUpdated) {
}
