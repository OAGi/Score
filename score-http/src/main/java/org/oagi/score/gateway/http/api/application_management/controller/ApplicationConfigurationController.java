package org.oagi.score.gateway.http.api.application_management.controller;

import org.oagi.score.gateway.http.api.application_management.controller.payload.ApplicationConfigurationChangeRequest;
import org.oagi.score.gateway.http.api.application_management.model.ApplicationSettingsInfo;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApplicationConfigurationController {

    @Autowired
    private ApplicationConfigurationService service;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/application/settings", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplicationSettingsInfo getApplicationSettingsInfo(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return service.getApplicationSettingsInfo(sessionService.asScoreUser(user));
    }

    @RequestMapping(value = "/application/settings", method = RequestMethod.POST)
    public ResponseEntity updateApplicationSettingsInfo(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                        @RequestBody ApplicationSettingsInfo applicationSettingsInfo) {

        service.updateApplicationSettingsInfo(sessionService.asScoreUser(user), applicationSettingsInfo);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/application/{type}/enable", method = RequestMethod.POST)
    public ResponseEntity tenantEnable(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                       @PathVariable("type") String type) {
        ApplicationConfigurationChangeRequest request = new ApplicationConfigurationChangeRequest();
        switch (type) {
            case "tenant":
                request.setTenantEnabled(true);
                // Multi-tenant mode does not support the business term management.
                request.setBusinessTermEnabled(false);
                break;

            case "business-term":
                request.setBusinessTermEnabled(true);
                break;

            case "bie-inverse-mode":
                request.setBieInverseModeEnabled(true);
                break;

            case "functions-requiring-email-transmission":
                request.setFunctionsRequiringEmailTransmissionEnabled(true);
                break;

            default:
                throw new UnsupportedOperationException("Unregistered type: " + type);
        }

        service.changeApplicationConfiguration(sessionService.asScoreUser(user), request);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/application/{type}/disable", method = RequestMethod.POST)
    public ResponseEntity tenantDisable(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                        @PathVariable("type") String type) {
        ApplicationConfigurationChangeRequest request = new ApplicationConfigurationChangeRequest();
        switch (type) {
            case "tenant":
                request.setTenantEnabled(false);
                break;

            case "business-term":
                request.setBusinessTermEnabled(false);
                break;

            case "bie-inverse-mode":
                request.setBieInverseModeEnabled(false);
                break;

            case "functions-requiring-email-transmission":
                request.setFunctionsRequiringEmailTransmissionEnabled(false);
                break;

            default:
                throw new UnsupportedOperationException("Unregistered type: " + type);
        }

        service.changeApplicationConfiguration(sessionService.asScoreUser(user), request);

        return ResponseEntity.noContent().build();
    }

}
