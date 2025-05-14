package org.oagi.score.gateway.http.api.context_management.business_context.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.controller.payload.CreateBusinessContextRequest;
import org.oagi.score.gateway.http.api.context_management.business_context.controller.payload.CreateBusinessContextResponse;
import org.oagi.score.gateway.http.api.context_management.business_context.controller.payload.DiscardBusinessContextRequest;
import org.oagi.score.gateway.http.api.context_management.business_context.controller.payload.UpdateBusinessContextRequest;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.context_management.business_context.service.BusinessContextCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Business Context - Commands", description = "API for creating, updating, and deleting business contexts")
@RequestMapping("/business-contexts")
public class BusinessContextCommandController {

    @Autowired
    private BusinessContextCommandService businessContextCommandService;

    @Autowired
    private SessionService sessionService;

    @Operation(summary = "Create a new business context", description = "Creates a new business context and returns its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created business context"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping()
    public CreateBusinessContextResponse createBusinessContext(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request payload for creating a business context",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateBusinessContextRequest.class)))
            @RequestBody CreateBusinessContextRequest request) {

        BusinessContextId newBusinessContextId =
                businessContextCommandService.create(sessionService.asScoreUser(user), request);
        return new CreateBusinessContextResponse(newBusinessContextId, "success", "");
    }

    @Operation(summary = "Update an existing business context", description = "Updates the details of an existing business context.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated business context"),
            @ApiResponse(responseCode = "404", description = "Context scheme not found")
    })
    @PutMapping(value = "/{bizCtxId:[\\d]+}")
    public void updateBusinessContext(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            
            @Parameter(description = "The ID of the business context.")
            @PathVariable("bizCtxId") BusinessContextId businessContextId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request payload for updating a business context",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateBusinessContextRequest.class)))
            @RequestBody UpdateBusinessContextRequest request) {

        businessContextCommandService.update(sessionService.asScoreUser(user),
                request.withBusinessContextId(businessContextId));
    }

    @Operation(summary = "Delete a business context", description = "Deletes a specific business context by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted business context"),
            @ApiResponse(responseCode = "404", description = "Context scheme not found")
    })
    @DeleteMapping(value = "/{bizCtxId:[\\d]+}")
    public ResponseEntity<Void> discardBusinessContext(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the business context.")
            @PathVariable("bizCtxId") BusinessContextId businessContextId) {

        businessContextCommandService.discard(
                sessionService.asScoreUser(user), businessContextId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    @Operation(summary = "Delete multiple business contexts", description = "Deletes multiple business contexts by their IDs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted business contexts"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @DeleteMapping()
    public ResponseEntity<Void> discardBusinessContexts(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request payload containing a list of business context IDs to delete",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DiscardBusinessContextRequest.class)))
            @RequestBody DiscardBusinessContextRequest request) {

        businessContextCommandService.discard(
                sessionService.asScoreUser(user), request.businessContextIdList());
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    /**
     * Assigns a business context to a top-level ASBIEP.
     *
     * @param user              The authenticated user.
     * @param businessContextId The business context ID.
     * @param topLevelAsbiepId  The top-level ASBIEP ID.
     * @return HTTP 201 Created response.
     */
    @Operation(summary = "Assign a business context to a top-level ASBIEP")
    @PostMapping(value = "/{bizCtxId:[\\d]+}/assignments/{topLevelAsbiepId:[\\d]+}")
    public ResponseEntity<Void> assignBusinessContext(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The business context ID")
            @PathVariable("bizCtxId") BusinessContextId businessContextId,

            @Parameter(description = "The top-level ASBIEP ID")
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId) {

        businessContextCommandService.assignBusinessContext(
                sessionService.asScoreUser(user), businessContextId, topLevelAsbiepId);
        return ResponseEntity.status(201).build(); // HTTP 201 Created
    }

    /**
     * Unassigns a business context from a top-level ASBIEP.
     *
     * @param user              The authenticated user.
     * @param businessContextId The business context ID.
     * @param topLevelAsbiepId  The top-level ASBIEP ID.
     * @return HTTP 204 No Content response.
     */
    @Operation(summary = "Unassign a business context from a top-level ASBIEP")
    @DeleteMapping(value = "/{bizCtxId:[\\d]+}/assignments/{topLevelAsbiepId:[\\d]+}")
    public ResponseEntity<Void> unassignBusinessContext(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The business context ID")
            @PathVariable("bizCtxId") BusinessContextId businessContextId,

            @Parameter(description = "The top-level ASBIEP ID")
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId) {

        businessContextCommandService.unassignBusinessContext(
                sessionService.asScoreUser(user), businessContextId, topLevelAsbiepId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
