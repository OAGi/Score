package org.oagi.score.gateway.http.api.tenant.controller;

import org.oagi.score.gateway.http.api.tenant.data.Tenant;
import org.oagi.score.gateway.http.api.tenant.data.TenantBusinessCtxInfo;
import org.oagi.score.gateway.http.api.tenant.data.TenantListRequest;
import org.oagi.score.gateway.http.api.tenant.service.TenantService;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenantController {
	
	@Autowired
	private TenantService tenantService;	
	
	@RequestMapping(value = "/tenants", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
	public PageResponse<Tenant> getAllTenantRoles(@RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize){
		
		TenantListRequest tenantRequest = new TenantListRequest();
		tenantRequest.setName(name);
		
		PageRequest pageRequest = new PageRequest();
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        tenantRequest.setPageRequest(pageRequest);
        
		return tenantService.getAllTenantRoles(tenantRequest);
	}
	
	@RequestMapping(value = "/tenants", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity createTenant(@RequestBody String name){
		if(!name.isBlank()) {
			tenantService.createTenant(name);
			return ResponseEntity.accepted().build();
		}
		 return ResponseEntity.badRequest().build();
	}
	
	@RequestMapping(value = "/tenants/{tenantId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
	public Tenant getTenantInfo(@PathVariable("tenantId") Long tenantId){
		return tenantService.getTenantById(tenantId);
	}
	
	@RequestMapping(value = "/tenants/biz/{tenantId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
	public TenantBusinessCtxInfo getTenantBusinessCtxInfo(@PathVariable("tenantId") Long tenantId){
		return tenantService.getTenantBusinessCxtInfoById(tenantId);
	}
	
	@RequestMapping(value = "/tenants/contexts", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity updateTenantBusinessCtxInfo(@RequestBody TenantBusinessCtxInfo info ){
		if(info != null && info.getTenantId() != null) {
			tenantService.updateTenantBusinessContext(info);
			 return ResponseEntity.accepted().build();
		}
		 return ResponseEntity.badRequest().build();
	}
	
	@RequestMapping(value = "/tenants/users/{tenantId}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity connectUserToTenant(@PathVariable("tenantId") Long tenantId,
			@RequestBody Long appUserId ){
		if(appUserId != null) {
			tenantService.addUserToTenant(tenantId, appUserId);
			return ResponseEntity.accepted().build();
		}
		 return ResponseEntity.badRequest().build();
	}
	
	@RequestMapping(value = "/tenants/users/{tenantId}", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity disconnectUserFromTenant(@PathVariable("tenantId") Long tenantId,
			@RequestBody Long appUserId ){
		if(appUserId != null) {
			tenantService.deleteTenantUser(tenantId, appUserId);
			return ResponseEntity.noContent().build();
		}
		 return ResponseEntity.badRequest().build();
	}
}
