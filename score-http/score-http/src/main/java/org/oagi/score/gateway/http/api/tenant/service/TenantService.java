package org.oagi.score.gateway.http.api.tenant.service;

import java.util.List;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.tenant.data.Tenant;
import org.oagi.score.gateway.http.api.tenant.data.TenantListRequest;
import org.oagi.score.repo.component.tenant.TenantRepository;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TenantService {
	
	@Autowired
	private TenantRepository tenantRepository;
	
	public List<ULong> getUserTenantsRoleByUserId(ULong userId) {
		return tenantRepository.getUserTenantsRoleByUserId(userId);
	}
	
	public List<String> getTenantNameByBusinessCtxId(Long businessCtxId){
		return tenantRepository.getTenantNameByBusinessCtxId(businessCtxId);
	}
	
	public PageResponse<Tenant> getAllTenantRoles(TenantListRequest tenantRequest){
		return tenantRepository.getAllTenantsRole(tenantRequest);
	}
	
	public void createTenant(String name) {
	    tenantRepository.createTenant(name);
	}
	

	public void updateTenant(Long tenantId, String name) {
	    tenantRepository.updateTenant(tenantId, name);
	}
	
	public Tenant getTenantById(Long tenantId) {
		return tenantRepository.getTenantById(tenantId);
	}
	
	public void addUserToTenant(Long tenantId, Long appUserId) {
		tenantRepository.addUserToTenant(tenantId, appUserId);
	}
	
	public void deleteTenantUser(Long tenantId, Long appUserId) {
		tenantRepository.deleteTenantUser(tenantId, appUserId);
	}
	
	public void addBusinessCtxToTenant(Long tenantId, Long businessCtxId) {
		tenantRepository.addBusinessCtxToTenant(tenantId, businessCtxId);
	}
	
	public void deleteTenantBusinessCtx(Long tenantId, Long businessCtxId) {
		tenantRepository.deleteTenantBusinessCtx(tenantId, businessCtxId);
	}
}
