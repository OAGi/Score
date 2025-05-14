package org.oagi.score.gateway.http.api.tenant_management.service;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TenantCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    public TenantId createTenant(ScoreUser requester, String name) {
        var command = repositoryFactory.tenantCommandRepository(requester);
        return command.createTenant(name);
    }

    public boolean updateTenant(
            ScoreUser requester, TenantId tenantId, String name) {
        var command = repositoryFactory.tenantCommandRepository(requester);
        return command.updateTenant(tenantId, name);
    }

    public void deleteTenant(
            ScoreUser requester, TenantId tenantId) {
        var command = repositoryFactory.tenantCommandRepository(requester);
        command.deleteTenant(tenantId);
    }

    public void addUserToTenant(
            ScoreUser requester, TenantId tenantId, UserId appUserId) {
        var command = repositoryFactory.tenantCommandRepository(requester);
        command.addUserToTenant(tenantId, appUserId);
    }

    public void deleteTenantUser(
            ScoreUser requester, TenantId tenantId, UserId appUserId) {
        var command = repositoryFactory.tenantCommandRepository(requester);
        command.deleteTenantUser(tenantId, appUserId);
    }

    public void addBusinessCtxToTenant(
            ScoreUser requester, TenantId tenantId, BusinessContextId businessCtxId) {
        var command = repositoryFactory.tenantCommandRepository(requester);
        command.addBusinessCtxToTenant(tenantId, businessCtxId);
    }

    public void deleteTenantBusinessCtx(
            ScoreUser requester, TenantId tenantId, BusinessContextId businessCtxId) {
        var command = repositoryFactory.tenantCommandRepository(requester);
        command.deleteTenantBusinessCtx(tenantId, businessCtxId);
    }

}
