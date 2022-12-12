package org.oagi.score.gateway.http.api.tenant.controller;

import org.oagi.score.gateway.http.api.tenant.data.Tenant;
import org.oagi.score.gateway.http.api.tenant.data.TenantInfo;
import org.oagi.score.gateway.http.api.tenant.data.TenantListRequest;
import org.oagi.score.gateway.http.api.tenant.service.TenantService;
import org.oagi.score.gateway.http.app.configuration.ConfigurationService;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

	@Autowired
	private ConfigurationService configService;

	@RequestMapping(value = "/tenants", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public PageResponse<Tenant> getAllTenantRoles(@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "sortDirection") String sortDirection, @RequestParam(name = "pageIndex") int pageIndex,
			@RequestParam(name = "pageSize") int pageSize) {

		if (!configService.isTenantInstance()) {
			throw new AccessDeniedException("Unauthorised Access!");
		}

		TenantListRequest tenantRequest = new TenantListRequest();
		tenantRequest.setName(name);

		PageRequest pageRequest = new PageRequest();
		pageRequest.setSortDirection(sortDirection);
		pageRequest.setPageIndex(pageIndex);
		pageRequest.setPageSize(pageSize);
		tenantRequest.setPageRequest(pageRequest);

		return tenantService.getAllTenantRoles(tenantRequest);
	}

	@RequestMapping(value = "/tenants", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity createTenant(@RequestBody String name) {
		if (!configService.isTenantInstance()) {
			throw new AccessDeniedException("Unauthorised Access!");
		}
		if (!name.isBlank()) {
			if (tenantService.createTenant(name)) {
				return ResponseEntity.status(HttpStatus.CREATED).build();
			}
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).build();
	}

	@RequestMapping(value = "/tenants/{tenantId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity deleteTenantInfo(@PathVariable("tenantId") Long tenantId) {
		if (!configService.isTenantInstance()) {
			throw new AccessDeniedException("Unauthorised Access!");
		}

		tenantService.deleteTenant(tenantId);
		return ResponseEntity.accepted().build();
	}

	@RequestMapping(value = "/tenants/{tenantId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity updateTenantInfo(@PathVariable("tenantId") Long tenantId, @RequestBody String name) {
		if (!configService.isTenantInstance()) {
			throw new AccessDeniedException("Unauthorised Access!");
		}
		if (!name.isBlank()) {
			if (tenantService.updateTenant(tenantId, name)) {
				return ResponseEntity.status(HttpStatus.ACCEPTED).build();
			}
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).build();
	}

	@RequestMapping(value = "/tenants/{tenantId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public TenantInfo getTenantInfo(@PathVariable("tenantId") Long tenantId) {
		if (!configService.isTenantInstance()) {
			throw new AccessDeniedException("Unauthorised Access!");
		}
		return tenantService.getTenantById(tenantId);
	}

	@RequestMapping(value = "/tenants/users/{tenantId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity connectUserToTenant(@PathVariable("tenantId") Long tenantId, @RequestBody Long appUserId) {
		if (!configService.isTenantInstance()) {
			throw new AccessDeniedException("Unauthorised Access!");
		}
		if (appUserId != null) {
			tenantService.addUserToTenant(tenantId, appUserId);
			return ResponseEntity.accepted().build();
		}
		return ResponseEntity.badRequest().build();
	}

	@RequestMapping(value = "/tenants/users/{tenantId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity disconnectUserFromTenant(@PathVariable("tenantId") Long tenantId,
			@RequestBody Long appUserId) {
		if (!configService.isTenantInstance()) {
			throw new AccessDeniedException("Unauthorised Access!");
		}
		if (appUserId != null) {
			tenantService.deleteTenantUser(tenantId, appUserId);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.badRequest().build();
	}

	@RequestMapping(value = "/tenants/bis-ctx/{tenantId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity connectBusinessCtxToTenant(@PathVariable("tenantId") Long tenantId,
			@RequestBody Long businessCtxId) {
		if (!configService.isTenantInstance()) {
			throw new AccessDeniedException("Unauthorised Access!");
		}
		if (businessCtxId != null) {
			tenantService.addBusinessCtxToTenant(tenantId, businessCtxId);
			return ResponseEntity.accepted().build();
		}
		return ResponseEntity.badRequest().build();
	}

	@RequestMapping(value = "/tenants/bis-ctx/{tenantId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity disconnectBusinessCtxFromTenant(@PathVariable("tenantId") Long tenantId,
			@RequestBody Long businessCtxId) {
		if (!configService.isTenantInstance()) {
			throw new AccessDeniedException("Unauthorised Access!");
		}
		if (businessCtxId != null) {
			tenantService.deleteTenantBusinessCtx(tenantId, businessCtxId);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.badRequest().build();
	}
}
