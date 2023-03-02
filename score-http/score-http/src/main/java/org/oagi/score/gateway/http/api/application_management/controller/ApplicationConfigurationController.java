package org.oagi.score.gateway.http.api.application_management.controller;

import org.oagi.score.gateway.http.api.application_management.data.ApplicationConfigurationChangeRequest;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationConfigurationController {

    @Autowired
    private ApplicationConfigurationService service;

    @RequestMapping(value = "/application/tenant/enable", method = RequestMethod.POST)
    public ResponseEntity tenantEnable(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        ApplicationConfigurationChangeRequest request = new ApplicationConfigurationChangeRequest();
        request.setTenantEnabled(true);

        service.changeApplicationConfiguration(user, request);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/application/tenant/disable", method = RequestMethod.POST)
    public ResponseEntity tenantDisable(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        ApplicationConfigurationChangeRequest request = new ApplicationConfigurationChangeRequest();
        request.setTenantEnabled(false);

        service.changeApplicationConfiguration(user, request);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/application/business-term/enable", method = RequestMethod.POST)
    public ResponseEntity businessTermEnable(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        ApplicationConfigurationChangeRequest request = new ApplicationConfigurationChangeRequest();
        request.setBusinessTermEnabled(true);

        service.changeApplicationConfiguration(user, request);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/application/business-term/disable", method = RequestMethod.POST)
    public ResponseEntity businessTermDisable(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        ApplicationConfigurationChangeRequest request = new ApplicationConfigurationChangeRequest();
        request.setBusinessTermEnabled(false);

        service.changeApplicationConfiguration(user, request);

        return ResponseEntity.noContent().build();
    }

}
