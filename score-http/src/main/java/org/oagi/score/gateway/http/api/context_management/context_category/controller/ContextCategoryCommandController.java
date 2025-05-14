package org.oagi.score.gateway.http.api.context_management.context_category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.context_management.context_category.controller.payload.CreateContextCategoryRequest;
import org.oagi.score.gateway.http.api.context_management.context_category.controller.payload.CreateContextCategoryResponse;
import org.oagi.score.gateway.http.api.context_management.context_category.controller.payload.DiscardContextCategoryRequest;
import org.oagi.score.gateway.http.api.context_management.context_category.controller.payload.UpdateContextCategoryRequest;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_category.service.ContextCategoryCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Context Category - Commands", description = "API for creating, updating, and deleting context categories")
@RequestMapping("/context-categories")
public class ContextCategoryCommandController {

    @Autowired
    private ContextCategoryCommandService contextCategoryCommandService;

    @Autowired
    private SessionService sessionService;

    @Operation(summary = "Create a new context category", description = "Creates a new context category and returns its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created context category"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping()
    public CreateContextCategoryResponse createContextCategory(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request payload for creating a context category",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateContextCategoryRequest.class)))
            @RequestBody CreateContextCategoryRequest request) {

        ContextCategoryId newContextCategoryId =
                contextCategoryCommandService.create(sessionService.asScoreUser(user), request);
        return new CreateContextCategoryResponse(newContextCategoryId, "success", "");
    }

    @Operation(summary = "Update an existing context category", description = "Updates the details of an existing context category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated context category"),
            @ApiResponse(responseCode = "404", description = "Context scheme not found")
    })
    @PutMapping(value = "/{ctxCategoryId:[\\d]+}")
    public void updateContextCategory(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the context category.")
            @PathVariable("ctxCategoryId") ContextCategoryId contextCategoryId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request payload for updating a context category",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateContextCategoryRequest.class)))
            @RequestBody UpdateContextCategoryRequest request) {

        contextCategoryCommandService.update(
                sessionService.asScoreUser(user), request.withContextCategoryId(contextCategoryId));
    }

    @Operation(summary = "Delete a context category", description = "Deletes a specific context category by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted context category"),
            @ApiResponse(responseCode = "404", description = "Context scheme not found")
    })
    @DeleteMapping(value = "/{ctxCategoryId:[\\d]+}")
    public ResponseEntity<Void> discardContextCategory(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the context category.")
            @PathVariable("ctxCategoryId") ContextCategoryId contextCategoryId) {

        contextCategoryCommandService.discard(
                sessionService.asScoreUser(user), contextCategoryId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    @Operation(summary = "Delete multiple context categories", description = "Deletes multiple context categories by their IDs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted context categories"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @DeleteMapping()
    public ResponseEntity<Void> discardContextCategories(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request payload containing a list of context category IDs to delete",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DiscardContextCategoryRequest.class)))
            @RequestBody DiscardContextCategoryRequest request) {

        contextCategoryCommandService.discard(
                sessionService.asScoreUser(user), request.contextCategoryIdList());
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

}
