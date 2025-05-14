package org.oagi.score.gateway.http.api.tenant_management.service;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.controller.payload.TenantListRequest;
import org.oagi.score.gateway.http.api.tenant_management.model.Tenant;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantInfo;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TenantQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    public PageResponse<Tenant> getAllTenantRoles(
            ScoreUser requester, TenantListRequest tenantRequest) {
        var query = repositoryFactory.tenantQueryRepository(requester);
        return query.getAllTenantsRole(tenantRequest);
    }

    public TenantInfo getTenantById(
            ScoreUser requester, TenantId tenantId) {
        var query = repositoryFactory.tenantQueryRepository(requester);
        return query.getTenantById(tenantId);
    }

    public List<TenantId> getUserTenantsRoleByUser(ScoreUser requester) {
        return getUserTenantsRoleByUserId(requester, requester.userId());
    }

    public List<TenantId> getUserTenantsRoleByUserId(
            ScoreUser requester, UserId userId) {
        var query = repositoryFactory.tenantQueryRepository(requester);
        return query.getUserTenantsRoleByUserId(userId);
    }

    public List<String> getTenantNameByBusinessCtxId(
            ScoreUser requester, BusinessContextId businessCtxId) {
        var query = repositoryFactory.tenantQueryRepository(requester);
        return query.getTenantNameByBusinessCtxId(businessCtxId);
    }

}
