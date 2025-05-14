package org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria;

import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

/**
 * Criteria for filtering business contexts in a list.
 * This record includes various fields to filter business contexts by name, tenant status,
 * connection status, business context editing status, and last updated timestamps.
 */
public record BusinessContextListFilterCriteria(String name,
                                                boolean tenantEnabled, TenantId tenantId, boolean notConnectedToTenant,
                                                boolean bieEditing,
                                                Collection<TenantId> userTenantIdList,
                                                Collection<String> updaterLoginIdSet,
                                                DateRangeCriteria lastUpdatedTimestampRange) {

    /**
     * Creates a new instance of BusinessContextListFilterCriteria with the updated user tenant ID list.
     *
     * @param userTenantIdList The updated list of tenant IDs for the user.
     * @return A new BusinessContextListFilterCriteria with the updated user tenant ID list.
     */
    public BusinessContextListFilterCriteria withUserTenantIdList(Collection<TenantId> userTenantIdList) {
        return new BusinessContextListFilterCriteria(name,
                tenantEnabled, tenantId, notConnectedToTenant, bieEditing, userTenantIdList,
                updaterLoginIdSet, lastUpdatedTimestampRange);
    }

}
