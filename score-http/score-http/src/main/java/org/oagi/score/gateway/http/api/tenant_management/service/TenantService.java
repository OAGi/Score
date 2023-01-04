package org.oagi.score.gateway.http.api.tenant_management.service;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.tenant_management.data.Tenant;
import org.oagi.score.gateway.http.api.tenant_management.data.TenantInfo;
import org.oagi.score.gateway.http.api.tenant_management.data.TenantListRequest;
import org.oagi.score.repo.component.tenant.TenantRepository;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
@Transactional
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    public List<ULong> getUserTenantsRoleByUserId(BigInteger userId) {
        return tenantRepository.getUserTenantsRoleByUserId(userId);
    }

    public List<String> getTenantNameByBusinessCtxId(BigInteger businessCtxId) {
        return tenantRepository.getTenantNameByBusinessCtxId(businessCtxId);
    }

    public PageResponse<Tenant> getAllTenantRoles(TenantListRequest tenantRequest) {
        return tenantRepository.getAllTenantsRole(tenantRequest);
    }

    public boolean createTenant(String name) {
        return tenantRepository.createTenant(name);
    }

    public boolean updateTenant(BigInteger tenantId, String name) {
        return tenantRepository.updateTenant(tenantId, name);
    }

    public TenantInfo getTenantById(BigInteger tenantId) {
        return tenantRepository.getTenantById(tenantId);
    }

    public void deleteTenant(BigInteger tenantId) {
        tenantRepository.deleteTenant(tenantId);
    }

    public void addUserToTenant(BigInteger tenantId, BigInteger appUserId) {
        tenantRepository.addUserToTenant(tenantId, appUserId);
    }

    public void deleteTenantUser(BigInteger tenantId, BigInteger appUserId) {
        tenantRepository.deleteTenantUser(tenantId, appUserId);
    }

    public void addBusinessCtxToTenant(BigInteger tenantId, BigInteger businessCtxId) {
        tenantRepository.addBusinessCtxToTenant(tenantId, businessCtxId);
    }

    public void deleteTenantBusinessCtx(BigInteger tenantId, BigInteger businessCtxId) {
        tenantRepository.deleteTenantBusinessCtx(tenantId, businessCtxId);
    }

}
