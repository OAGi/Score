package org.oagi.score.gateway.http.api.context_management.context_scheme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.CreateContextSchemeRequest;
import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.CreateContextSchemeResponse;
import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.DiscardContextSchemeRequest;
import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.UpdateContextSchemeRequest;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.service.ContextSchemeCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Context Scheme - Commands", description = "API for creating, updating, and deleting context schemes")
@RequestMapping("/context-schemes")
public class ContextSchemeCommandController {

    @Autowired
    private ContextSchemeCommandService contextSchemeCommandService;

    @Autowired
    private SessionService sessionService;

    @Operation(summary = "Create a new context scheme", description = "Creates a new context scheme and returns its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created context scheme"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping()
    public CreateContextSchemeResponse createContextScheme(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request payload for creating a context scheme",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateContextSchemeRequest.class)))
            @RequestBody CreateContextSchemeRequest request) {

        ContextSchemeId newContextSchemeId =
                contextSchemeCommandService.create(sessionService.asScoreUser(user), request);
        return new CreateContextSchemeResponse(newContextSchemeId, "success", "");
    }

    @Operation(summary = "Update an existing context scheme", description = "Updates the details of an existing context scheme.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated context scheme"),
            @ApiResponse(responseCode = "404", description = "Context scheme not found")
    })
    @PutMapping(value = "/{ctxSchemeId:[\\d]+}")
    public void updateContextScheme(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the context scheme.")
            @PathVariable("ctxSchemeId") ContextSchemeId contextSchemeId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request payload for updating a context scheme",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateContextSchemeRequest.class)))
            @RequestBody UpdateContextSchemeRequest request) {

        contextSchemeCommandService.update(sessionService.asScoreUser(user),
                request.withContextSchemeId(contextSchemeId));
    }

    @Operation(summary = "Delete a context scheme", description = "Deletes a specific context scheme by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted context scheme"),
            @ApiResponse(responseCode = "404", description = "Context scheme not found")
    })
    @DeleteMapping(value = "/{ctxSchemeId:[\\d]+}")
    public ResponseEntity<Void> discardContextScheme(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the context scheme.")
            @PathVariable("ctxSchemeId") ContextSchemeId contextSchemeId) {

        contextSchemeCommandService.discard(sessionService.asScoreUser(user), contextSchemeId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    @Operation(summary = "Delete multiple context schemes", description = "Deletes multiple context schemes by their IDs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted context schemes"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @DeleteMapping()
    public ResponseEntity<Void> discardContextSchemes(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request payload containing a list of context scheme IDs to delete",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DiscardContextSchemeRequest.class)))
            @RequestBody DiscardContextSchemeRequest request) {

        contextSchemeCommandService.discard(
                sessionService.asScoreUser(user), request.contextSchemeIdList());
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
