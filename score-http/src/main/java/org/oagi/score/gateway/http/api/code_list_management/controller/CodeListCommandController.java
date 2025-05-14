package org.oagi.score.gateway.http.api.code_list_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.agency_id_management.controller.payload.TransferAgencyIDListOwnershipRequest;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.code_list_management.controller.payload.*;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListCommandService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "Code List - Commands", description = "API for creating, updating, and deleting code lists")
@RequestMapping("/code-lists")
public class CodeListCommandController {

    @Autowired
    private CodeListCommandService codeListCommandService;

    @Autowired
    private SessionService sessionService;

    @PostMapping
    public CreateCodeListResponse createCodeList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CreateCodeListRequest request) {

        CodeListManifestId newCodeListManifestId =
                codeListCommandService.create(sessionService.asScoreUser(user), request);
        return new CreateCodeListResponse(newCodeListManifestId, "success", "");
    }

    @PutMapping(value = "/{codeListManifestId:[\\d]+}")
    public void updateCodeList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                               @PathVariable("codeListManifestId") CodeListManifestId codeListManifestId,
                               @RequestBody UpdateCodeListRequest request) {

        codeListCommandService.update(
                sessionService.asScoreUser(user), request.withCodeListManifestId(codeListManifestId));
    }

    @PatchMapping(value = "/{codeListManifestId:[\\d]+}/state")
    public void updateCodeListState(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("codeListManifestId") CodeListManifestId codeListManifestId,
            @RequestBody Map<String, String> request) {

        String toState = request.get("toState");
        codeListCommandService.updateState(
                sessionService.asScoreUser(user), codeListManifestId, CcState.valueOf(toState));
    }

    @PatchMapping(value = "/{codeListManifestId:[\\d]+}/revise")
    public void reviseCodeList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("codeListManifestId") CodeListManifestId codeListManifestId) {

        codeListCommandService.revise(sessionService.asScoreUser(user), codeListManifestId);
    }

    @PatchMapping(value = "/{codeListManifestId:[\\d]+}/cancel")
    public void cancelCodeList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("codeListManifestId") CodeListManifestId codeListManifestId) {

        codeListCommandService.cancel(sessionService.asScoreUser(user), codeListManifestId);
    }

    @PatchMapping(value = "/{codeListManifestId:[\\d]+}/mark-as-deleted")
    public void markCodeListAsDeleted(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the agency ID list manifest to be deleted", required = true)
            @PathVariable("codeListManifestId")
            CodeListManifestId codeListManifestId) {

        codeListCommandService.markAsDeleted(sessionService.asScoreUser(user), codeListManifestId);
    }

    @PatchMapping(value = "/mark-as-deleted")
    public void markCodeListsAsDeleted(@AuthenticationPrincipal
                                       AuthenticatedPrincipal user,

                                       @RequestBody
                                       UpdateCodeListManifestWithMultipleIdListRequest request) {

        request.codeListManifestIds().forEach(codeListManifestId -> {
            markCodeListAsDeleted(user, codeListManifestId);
        });
    }

    @DeleteMapping
    public void purgeCodeLists(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestBody
            UpdateCodeListManifestWithMultipleIdListRequest request) {

        request.codeListManifestIds().forEach(codeListManifestId -> {
            codeListCommandService.purge(sessionService.asScoreUser(user), codeListManifestId);
        });
    }

    @PatchMapping(value = "/restore")
    public void restoreCodeLists(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestBody
            UpdateCodeListManifestWithMultipleIdListRequest request) {

        request.codeListManifestIds().forEach(codeListManifestId -> {
            codeListCommandService.restore(sessionService.asScoreUser(user), codeListManifestId);
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
    @PatchMapping("/{codeListManifestId:[\\d]+}/transfer")
    public void transferOwnership(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the agency ID list manifest to transfer", required = true)
            @PathVariable("codeListManifestId") CodeListManifestId codeListManifestId,

            @RequestBody TransferAgencyIDListOwnershipRequest request) {

        codeListCommandService.transferOwnership(sessionService.asScoreUser(user),
                sessionService.getScoreUserByUsername(request.targetLoginId()),
                codeListManifestId);
    }

    @PostMapping(value = "/{codeListManifestId:[\\d]+}/uplift")
    public CodeListUpliftingResponse upliftCodeList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @PathVariable("codeListManifestId")
            CodeListManifestId codeListManifestId,

            @Parameter(description = "The ID of the release.")
            @RequestParam(name = "targetReleaseId") ReleaseId targetReleaseId) {

        return codeListCommandService.uplift(sessionService.asScoreUser(user), codeListManifestId, targetReleaseId);
    }

}
