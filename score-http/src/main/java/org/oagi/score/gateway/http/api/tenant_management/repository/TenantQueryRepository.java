package org.oagi.score.gateway.http.api.tenant_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.controller.payload.TenantListRequest;
import org.oagi.score.gateway.http.api.tenant_management.model.Tenant;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantInfo;
import org.oagi.score.gateway.http.common.model.PageResponse;

import java.util.List;

public interface TenantQueryRepository {

    List<TenantId> getUserTenantsRoleByUserId(UserId userId);

    TenantInfo getTenantById(TenantId tenantId);

    List<String> getTenantNameByBusinessCtxId(BusinessContextId businessCtxId);

    PageResponse<Tenant> getAllTenantsRole(TenantListRequest tenantRequest);

}
