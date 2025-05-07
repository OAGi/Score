package org.oagi.score.gateway.http.api.tenant_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantCommandService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Tenant - Commands", description = "API for creating, updating, and deleting tenants")
@RequestMapping("/tenants")
public class TenantCommandController {

    @Autowired
    private TenantCommandService tenantService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ApplicationConfigurationService configService;

    @PostMapping()
    public ResponseEntity createTenant(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody String name) {

        ScoreUser requester = sessionService.asScoreUser(user);
        if (!configService.isTenantEnabled(requester)) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (!name.isBlank()) {
            TenantId tenantId = tenantService.createTenant(requester, name);
            if (tenantId != null) {
                return ResponseEntity.status(HttpStatus.CREATED).build();
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @DeleteMapping(value = "/{tenantId:[\\d]+}")
    public ResponseEntity deleteTenantInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("tenantId") TenantId tenantId) {

        ScoreUser requester = sessionService.asScoreUser(user);
        if (!configService.isTenantEnabled(requester)) {
            throw new AccessDeniedException("Unauthorized Access!");
        }

        tenantService.deleteTenant(requester, tenantId);
        return ResponseEntity.accepted().build();
    }

    @PutMapping(value = "/{tenantId:[\\d]+}")
    public ResponseEntity updateTenantInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("tenantId") TenantId tenantId,
            @RequestBody String name) {

        ScoreUser requester = sessionService.asScoreUser(user);
        if (!configService.isTenantEnabled(requester)) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (!name.isBlank()) {
            if (tenantService.updateTenant(requester, tenantId, name)) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).build();
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PostMapping(value = "/{tenantId:[\\d]+}/users/{userId:[\\d]+}")
    public ResponseEntity connectUserToTenant(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("tenantId") TenantId tenantId,
            @PathVariable("userId") UserId appUserId) {

        ScoreUser requester = sessionService.asScoreUser(user);
        if (!configService.isTenantEnabled(requester)) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (appUserId != null) {
            tenantService.addUserToTenant(requester, tenantId, appUserId);
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping(value = "/{tenantId:[\\d]+}/users/{userId:[\\d]+}")
    public ResponseEntity disconnectUserFromTenant(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("tenantId") TenantId tenantId,
            @PathVariable("userId") UserId appUserId) {

        ScoreUser requester = sessionService.asScoreUser(user);
        if (!configService.isTenantEnabled(requester)) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (appUserId != null) {
            tenantService.deleteTenantUser(requester, tenantId, appUserId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping(value = "/{tenantId:[\\d]+}/business-contexts/{bizCtxId:[\\d]+}")
    public ResponseEntity connectBusinessCtxToTenant(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("tenantId") TenantId tenantId,
            @PathVariable("bizCtxId") BusinessContextId businessCtxId) {

        ScoreUser requester = sessionService.asScoreUser(user);
        if (!configService.isTenantEnabled(requester)) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (businessCtxId != null) {
            tenantService.addBusinessCtxToTenant(requester, tenantId, businessCtxId);
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping(value = "/{tenantId:[\\d]+}/business-contexts/{bizCtxId:[\\d]+}")
    public ResponseEntity disconnectBusinessCtxFromTenant(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("tenantId") TenantId tenantId,
            @PathVariable("bizCtxId") BusinessContextId businessCtxId) {

        ScoreUser requester = sessionService.asScoreUser(user);
        if (!configService.isTenantEnabled(requester)) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        if (businessCtxId != null) {
            tenantService.deleteTenantBusinessCtx(requester, tenantId, businessCtxId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
