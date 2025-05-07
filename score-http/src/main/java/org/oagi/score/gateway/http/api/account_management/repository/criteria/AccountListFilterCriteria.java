package org.oagi.score.gateway.http.api.account_management.repository.criteria;

import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;

import java.util.Collection;

public record AccountListFilterCriteria(
        String loginId,
        String name,
        String organization,
        Collection<String> roles,
        Boolean enabled,
        Boolean excludeRequester,
        Boolean excludeSSO,

        boolean tenantEnabled, TenantId tenantId, boolean notConnectedToTenant,
        Collection<BusinessContextId> businessContextIdList) {
}
