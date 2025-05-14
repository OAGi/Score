package org.oagi.score.gateway.http.api.namespace_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.namespace_management.controller.payload.CreateNamespaceRequest;
import org.oagi.score.gateway.http.api.namespace_management.controller.payload.CreateNamespaceResponse;
import org.oagi.score.gateway.http.api.namespace_management.controller.payload.TransferNamespaceOwnershipRequest;
import org.oagi.score.gateway.http.api.namespace_management.controller.payload.UpdateNamespaceRequest;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.service.NamespaceCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Namespace - Commands", description = "API for creating, updating, and deleting namespaceIds")
@RequestMapping("/namespaces")
public class NamespaceCommandController {

    @Autowired
    private NamespaceCommandService namespaceCommandService;

    @Autowired
    private SessionService sessionService;

    @Operation(summary = "Create a new namespace", description = "Creates a new namespace and returns its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Namespace created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public CreateNamespaceResponse createNamespace(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CreateNamespaceRequest request) {

        NamespaceId newNamespaceId = namespaceCommandService.create(sessionService.asScoreUser(user), request);
        return new CreateNamespaceResponse(newNamespaceId, "success", "");
    }

    @Operation(summary = "Update an existing namespace", description = "Updates a namespace with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Namespace updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Namespace not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{namespaceId:[\\d]+}")
    public void updateNamespace(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the namespace to be updated", required = true)
            @PathVariable("namespaceId") NamespaceId namespaceId,

            @RequestBody UpdateNamespaceRequest request) {

        namespaceCommandService.update(sessionService.asScoreUser(user), request.withNamespaceId(namespaceId));
    }

    @Operation(summary = "Delete a namespace by ID", description = "Deletes a namespace with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Namespace deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Namespace not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{namespaceId:[\\d]+}")
    public void discardNamespace(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the namespace to be deleted", required = true)
            @PathVariable("namespaceId") NamespaceId namespaceId) {

        namespaceCommandService.discard(sessionService.asScoreUser(user), namespaceId);
    }

    @Operation(summary = "Transfer ownership of a namespace",
            description = "Transfers ownership of the specified namespace to another user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ownership transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission"),
            @ApiResponse(responseCode = "404", description = "Record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{namespaceId:[\\d]+}/transfer")
    public void transferOwnership(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the namespace to transfer", required = true)
            @PathVariable("namespaceId") NamespaceId namespaceId,

            @RequestBody TransferNamespaceOwnershipRequest request) {

        namespaceCommandService.transferOwnership(sessionService.asScoreUser(user),
                sessionService.getScoreUserByUsername(request.targetLoginId()),
                namespaceId);
    }

}
