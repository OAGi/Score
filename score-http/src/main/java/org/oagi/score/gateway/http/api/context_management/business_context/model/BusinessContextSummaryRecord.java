package org.oagi.score.gateway.http.api.context_management.business_context.model;

import org.oagi.score.gateway.http.common.model.Guid;

/**
 * Represents a summary of a business context, including the business context ID, GUID, and meaning.
 */
public record BusinessContextSummaryRecord(BusinessContextId businessContextId,
                                           Guid guid,
                                           String name) {
}
