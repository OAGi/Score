package org.oagi.score.gateway.http.api.application_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.application_management.controller.payload.ApplicationConfigurationChangeRequest;
import org.oagi.score.gateway.http.api.application_management.controller.payload.FilenameExpressionValidationRequest;
import org.oagi.score.gateway.http.api.application_management.model.ApplicationSettingsInfo;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles mutating application-configuration requests.
 * <p>
 * This controller exposes administrative endpoints for:
 * updating stored application settings,
 * validating filename-expression changes before persistence,
 * and toggling boolean application features on or off.
 */
@RestController
@Tag(name = "Application Configuration - Commands", description = "API for updating application configuration")
@RequestMapping("/application")
public class ApplicationConfigurationCommandController {

    @Autowired
    private ApplicationConfigurationCommandService service;

    @Autowired
    private SessionService sessionService;

    /**
     * Persists the submitted application settings.
     *
     * @param user authenticated principal performing the update
     * @param applicationSettingsInfo settings payload to persist
     * @return {@code 204 No Content} when the update succeeds
     */
    @PostMapping(value = "/settings")
    public ResponseEntity updateApplicationSettingsInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody ApplicationSettingsInfo applicationSettingsInfo) {

        service.updateApplicationSettingsInfo(sessionService.asScoreUser(user), applicationSettingsInfo);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validates a filename expression pair without storing it.
     *
     * @param user authenticated principal performing the validation
     * @param type expression target type such as {@code bie-schema} or {@code bie-package-schema}
     * @param request expression payload to validate
     * @return {@code 204 No Content} when the expression is valid
     */
    @PostMapping(value = "/filename-expression/{type}/validate")
    public ResponseEntity validateFilenameExpression(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("type") String type,
            @RequestBody FilenameExpressionValidationRequest request) {

        service.validateFilenameExpression(sessionService.asScoreUser(user), type,
                request.getExpression(), request.getDuplicateHandlerExpression());
        return ResponseEntity.noContent().build();
    }

    /**
     * Enables a boolean application feature identified by {@code type}.
     *
     * @param user authenticated principal performing the update
     * @param type feature key to enable
     * @return {@code 204 No Content} when the feature has been enabled
     */
    @PostMapping(value = "/{type}/enable")
    public ResponseEntity enable(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
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

            case "browse-standard-mode":
                request.setBrowseStandardModeEnabled(true);
                break;

            default:
                throw new UnsupportedOperationException("Unregistered type: " + type);
        }

        service.changeApplicationConfiguration(sessionService.asScoreUser(user), request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Disables a boolean application feature identified by {@code type}.
     *
     * @param user authenticated principal performing the update
     * @param type feature key to disable
     * @return {@code 204 No Content} when the feature has been disabled
     */
    @PostMapping(value = "/{type}/disable")
    public ResponseEntity disable(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
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

            case "browse-standard-mode":
                request.setBrowseStandardModeEnabled(false);
                break;

            default:
                throw new UnsupportedOperationException("Unregistered type: " + type);
        }

        service.changeApplicationConfiguration(sessionService.asScoreUser(user), request);
        return ResponseEntity.noContent().build();
    }

}
