package org.oagi.score.gateway.http.api.application_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.application_management.controller.payload.FilenameExpressionPreviewRequest;
import org.oagi.score.gateway.http.api.application_management.controller.payload.FilenameExpressionPreviewResponse;
import org.oagi.score.gateway.http.api.application_management.controller.payload.FilenameExpressionSuggestRequest;
import org.oagi.score.gateway.http.api.application_management.controller.payload.FilenameExpressionSuggestResponse;
import org.oagi.score.gateway.http.api.application_management.controller.payload.FilenameExpressionValidationRequest;
import org.oagi.score.gateway.http.api.application_management.model.ApplicationSettingsInfo;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationQueryService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Handles read-oriented application-configuration requests.
 * <p>
 * This controller exposes endpoints for retrieving persisted settings
 * and generating preview values for filename expressions without mutating state.
 */
@RestController
@Tag(name = "Application Configuration - Queries", description = "API for retrieving application configuration")
@RequestMapping("/application")
public class ApplicationConfigurationQueryController {

    @Autowired
    private ApplicationConfigurationQueryService service;

    @Autowired
    private SessionService sessionService;

    /**
     * Returns the effective application settings for the authenticated user context.
     *
     * @param user authenticated principal requesting the settings
     * @return current application settings
     */
    @GetMapping(value = "/settings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplicationSettingsInfo getApplicationSettingsInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal user) {
        return service.getApplicationSettingsInfo(sessionService.asScoreUser(user));
    }

    /**
     * Builds a sample filename preview for the supplied expression pair.
     *
     * @param user authenticated principal requesting the preview
     * @param type expression target type such as {@code bie-schema} or {@code bie-package-schema}
     * @param request expression payload to preview
     * @return sample filename values derived from the expression
     */
    @PostMapping(value = "/filename-expression/{type}/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public FilenameExpressionPreviewResponse previewFilenameExpression(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("type") String type,
            @RequestBody FilenameExpressionPreviewRequest request) {

        return service.previewFilenameExpression(sessionService.asScoreUser(user), type,
                request.getExpression(), request.getDuplicateHandlerExpression(),
                request.getSampleValues(), request.getSampleBusinessContextNames());
    }

    /**
     * Completions for the filename expression text at the caret.
     *
     * @param user authenticated principal requesting the suggestions
     * @param type expression target type such as {@code bie-schema} or {@code bie-package-schema}
     * @param request the text, the caret and whether it is the duplicate handler expression
     * @return the range to replace and the suggestions that fit the grammar there
     */
    @PostMapping(value = "/filename-expression/{type}/suggest", produces = MediaType.APPLICATION_JSON_VALUE)
    public FilenameExpressionSuggestResponse suggestFilenameExpression(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("type") String type,
            @RequestBody FilenameExpressionSuggestRequest request) {

        return service.suggestFilenameExpression(sessionService.asScoreUser(user), type,
                request.getText(), request.getCaret(), request.isDuplicateHandler());
    }

}
