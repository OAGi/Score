package org.oagi.score.gateway.http.api.tenant_management.controller;

import org.oagi.score.gateway.http.api.tenant_management.data.Tenant;
import org.oagi.score.gateway.http.api.tenant_management.data.TenantInfo;
import org.oagi.score.gateway.http.api.tenant_management.data.TenantListRequest;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantService;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ApplicationConfigurationService configService;

    @RequestMapping(value = "/tenants", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<Tenant> getAllTenantRoles(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        if (!configService.isTenantEnabled()) {
            throw new AccessDeniedException("Unauthorized Access!");
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

    @RequestMapping(value = "/tenants", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createTenant(@RequestBody String name) {
        if (!configService.isTenantEnabled()) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (!name.isBlank()) {
            if (tenantService.createTenant(name)) {
                return ResponseEntity.status(HttpStatus.CREATED).build();
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @RequestMapping(value = "/tenants/{tenantId}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteTenantInfo(@PathVariable("tenantId") BigInteger tenantId) {
        if (!configService.isTenantEnabled()) {
            throw new AccessDeniedException("Unauthorized Access!");
        }

        tenantService.deleteTenant(tenantId);
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/tenants/{tenantId}", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateTenantInfo(@PathVariable("tenantId") BigInteger tenantId,
                                           @RequestBody String name) {
        if (!configService.isTenantEnabled()) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (!name.isBlank()) {
            if (tenantService.updateTenant(tenantId, name)) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).build();
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @RequestMapping(value = "/tenants/{tenantId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TenantInfo getTenantInfo(@PathVariable("tenantId") BigInteger tenantId) {
        if (!configService.isTenantEnabled()) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        return tenantService.getTenantById(tenantId);
    }

    @RequestMapping(value = "/tenants/users/{tenantId}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity connectUserToTenant(@PathVariable("tenantId") BigInteger tenantId,
                                              @RequestBody BigInteger appUserId) {
        if (!configService.isTenantEnabled()) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (appUserId != null) {
            tenantService.addUserToTenant(tenantId, appUserId);
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = "/tenants/users/{tenantId}", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity disconnectUserFromTenant(@PathVariable("tenantId") BigInteger tenantId,
                                                   @RequestBody BigInteger appUserId) {
        if (!configService.isTenantEnabled()) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (appUserId != null) {
            tenantService.deleteTenantUser(tenantId, appUserId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = "/tenants/bis-ctx/{tenantId}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity connectBusinessCtxToTenant(@PathVariable("tenantId") BigInteger tenantId,
                                                     @RequestBody BigInteger businessCtxId) {
        if (!configService.isTenantEnabled()) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (businessCtxId != null) {
            tenantService.addBusinessCtxToTenant(tenantId, businessCtxId);
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = "/tenants/bis-ctx/{tenantId}", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity disconnectBusinessCtxFromTenant(@PathVariable("tenantId") BigInteger tenantId,
                                                          @RequestBody BigInteger businessCtxId) {
        if (!configService.isTenantEnabled()) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (businessCtxId != null) {
            tenantService.deleteTenantBusinessCtx(tenantId, businessCtxId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
