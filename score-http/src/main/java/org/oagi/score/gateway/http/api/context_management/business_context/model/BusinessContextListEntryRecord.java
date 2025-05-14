package org.oagi.score.gateway.http.api.context_management.business_context.model;

import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

/**
 * Represents a list entry for a business context, including the business context ID, GUID, name,
 * connected tenant names, status (whether it's in use), and the creation and last update information.
 */
public record BusinessContextListEntryRecord(BusinessContextId businessContextId,
                                             Guid guid,
                                             String name,
                                             String connectedTenantNames,
                                             boolean used,
                                             WhoAndWhen created,
                                             WhoAndWhen lastUpdated) {

    /**
     * Creates a new instance with updated connected tenant names.
     *
     * @param connectedTenantNames The updated connected tenant names.
     * @return A new BusinessContextListEntryRecord with the updated connected tenant names.
     */
    public BusinessContextListEntryRecord withConnectedTenantNames(String connectedTenantNames) {
        return new BusinessContextListEntryRecord(businessContextId, guid, name, connectedTenantNames, used, created, lastUpdated);
    }
}
