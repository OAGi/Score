package org.oagi.score.gateway.http.api.tenant_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;

public interface TenantCommandRepository {

    TenantId createTenant(String name);

    boolean updateTenant(TenantId tenantId, String name);

    void deleteTenant(TenantId tenantId);

    void addUserToTenant(TenantId tenantId, UserId appUserId);

    void deleteTenantUser(TenantId tenantId, UserId appUserId);

    void addBusinessCtxToTenant(TenantId tenantId, BusinessContextId businessCtxId);

    void deleteTenantBusinessCtx(TenantId tenantId, BusinessContextId businessCtxId);

}
