package org.oagi.score.gateway.http.api.context_management.business_context.model;

import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

/**
 * Represents the details of a business context.
 * This record contains the business context ID, GUID, meaning, status (whether it's in use),
 * and information about when it was created and last updated.
 */
public record BusinessContextDetailsRecord(BusinessContextId businessContextId,
                                           Guid guid,
                                           String name,
                                           boolean used,
                                           WhoAndWhen created,
                                           WhoAndWhen lastUpdated) {
}
