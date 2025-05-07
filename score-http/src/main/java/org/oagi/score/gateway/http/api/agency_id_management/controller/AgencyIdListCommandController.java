package org.oagi.score.gateway.http.api.agency_id_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.agency_id_management.controller.payload.*;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListCommandService;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "Agency ID List - Commands", description = "API for creating, updating, and deleting agency ID lists")
@RequestMapping("/agency-id-lists")
public class AgencyIdListCommandController {

    @Autowired
    private AgencyIdListCommandService agencyIdListCommandService;

    @Autowired
    private SessionService sessionService;

    @PostMapping
    public CreateAgencyIdListResponse createAgencyIdList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CreateAgencyIdListRequest request) {

        AgencyIdListManifestId newAgencyIdListManifestId =
                agencyIdListCommandService.create(sessionService.asScoreUser(user), request);
        return new CreateAgencyIdListResponse(newAgencyIdListManifestId, "success", "");
    }

    @PutMapping(value = "/{agencyIdListManifestId:[\\d]+}")
    public void updateAgencyIdList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                   @PathVariable("agencyIdListManifestId") AgencyIdListManifestId agencyIdListManifestId,
                                   @RequestBody UpdateAgencyIdListRequest request) {

        agencyIdListCommandService.update(
                sessionService.asScoreUser(user), request.withAgencyIdListManifestId(agencyIdListManifestId));
    }

    @PatchMapping(value = "/{agencyIdListManifestId:[\\d]+}/state")
    public void updateAgencyIdListState(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("agencyIdListManifestId") AgencyIdListManifestId agencyIdListManifestId,
            @RequestBody Map<String, String> request) {

        String toState = request.get("toState");
        agencyIdListCommandService.updateState(
                sessionService.asScoreUser(user), agencyIdListManifestId, CcState.valueOf(toState));
    }

    @PatchMapping(value = "/{agencyIdListManifestId:[\\d]+}/revise")
    public void reviseAgencyIdList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("agencyIdListManifestId") AgencyIdListManifestId agencyIdListManifestId) {

        agencyIdListCommandService.revise(sessionService.asScoreUser(user), agencyIdListManifestId);
    }

    @PatchMapping(value = "/{agencyIdListManifestId:[\\d]+}/cancel")
    public void cancelAgencyIdList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("agencyIdListManifestId") AgencyIdListManifestId agencyIdListManifestId) {

        agencyIdListCommandService.cancel(sessionService.asScoreUser(user), agencyIdListManifestId);
    }

    @PatchMapping(value = "/{agencyIdListManifestId:[\\d]+}/mark-as-deleted")
    public void markAgencyIdListAsDeleted(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the agency ID list manifest to be deleted", required = true)
            @PathVariable("agencyIdListManifestId")
            AgencyIdListManifestId agencyIdListManifestId) {

        agencyIdListCommandService.markAsDeleted(sessionService.asScoreUser(user), agencyIdListManifestId);
    }

    @PatchMapping(value = "/mark-as-deleted")
    public void markAgencyIdListsAsDeleted(@AuthenticationPrincipal
                                           AuthenticatedPrincipal user,

                                           @RequestBody
                                           UpdateAgencyIdListManifestWithMultipleIdListRequest request) {

        request.agencyIdListManifestIds().forEach(agencyIdListManifestId -> {
            markAgencyIdListAsDeleted(user, agencyIdListManifestId);
        });
    }

    @DeleteMapping
    public void purgeAgencyIdLists(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestBody
            UpdateAgencyIdListManifestWithMultipleIdListRequest request) {

        request.agencyIdListManifestIds().forEach(agencyIdListManifestId -> {
            agencyIdListCommandService.purge(sessionService.asScoreUser(user), agencyIdListManifestId);
        });
    }

    @PatchMapping(value = "/restore")
    public void restoreAgencyIdLists(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestBody
            UpdateAgencyIdListManifestWithMultipleIdListRequest request) {

        request.agencyIdListManifestIds().forEach(agencyIdListManifestId -> {
            agencyIdListCommandService.restore(sessionService.asScoreUser(user), agencyIdListManifestId);
        });
    }

    @Operation(summary = "Transfer ownership of an agency ID list",
            description = "Transfers ownership of the specified agency ID list to another user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ownership transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission"),
            @ApiResponse(responseCode = "404", description = "Record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{agencyIdListManifestId:[\\d]+}/transfer")
    public void transferOwnership(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the agency ID list manifest to transfer", required = true)
            @PathVariable("agencyIdListManifestId") AgencyIdListManifestId agencyIdListManifestId,

            @RequestBody TransferAgencyIDListOwnershipRequest request) {

        agencyIdListCommandService.transferOwnership(sessionService.asScoreUser(user),
                sessionService.getScoreUserByUsername(request.targetLoginId()),
                agencyIdListManifestId);
    }

}
