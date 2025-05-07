package org.oagi.score.gateway.http.api.cc_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.*;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.acc.AccCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.acc.AccSequenceUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.ascc.AsccCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.asccp.*;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bcc.BccCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bccp.BccpCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.dt.DtCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.service.CcCommandService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Core Component - Commands", description = "API for creating, updating, and deleting core components")
@RequestMapping("/core-components")
public class CcCommandController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CcCommandService ccCommandService;

    @Autowired
    private SessionService sessionService;

    @PostMapping(value = "/acc")
    public CcCreateResponse createAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody AccCreateRequest request) {

        AccManifestId manifestId = ccCommandService.createAcc(sessionService.asScoreUser(user), request);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @PatchMapping(value = "/acc/{accManifestId:[\\d]+}/state")
    public ResponseEntity updateAccState(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId,
            @RequestParam("state") CcState state) {

        boolean updated = ccCommandService.updateState(sessionService.asScoreUser(user), accManifestId, state);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }

    @PatchMapping(value = "/acc/{accManifestId:[\\d]+}/base")
    public ResponseEntity updateBasedAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId,
            @RequestParam(value = "basedAccManifestId", required = false) AccManifestId basedAccManifestId) {

        boolean updated = ccCommandService.updateBasedAccManifestId(sessionService.asScoreUser(user), accManifestId, basedAccManifestId);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }

    @PatchMapping(value = "/acc/{accManifestId:[\\d]+}/sequence")
    public void updateAccSequence(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId,
            @RequestBody AccSequenceUpdateRequest request) {

        ccCommandService.updateAccSequence(sessionService.asScoreUser(user), accManifestId,
                request.item(), request.after());
    }

    @DeleteMapping(value = "/acc/{accManifestId:[\\d]+}")
    public void purgeAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId) {

        ccCommandService.purge(sessionService.asScoreUser(user), accManifestId);
    }

    @PostMapping(value = "/acc/{accManifestId:[\\d]+}/ascc/{asccpManifestId:[\\d]+}")
    public CcCreateResponse appendAscc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId,
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId,
            @RequestParam(value = "pos", defaultValue = "-1") int pos) {

        AsccManifestId asccManifestId = ccCommandService.createAscc(
                sessionService.asScoreUser(user),
                AsccCreateRequest.builder(accManifestId, asccpManifestId).pos(pos).build());

        CcCreateResponse response = new CcCreateResponse();
        response.setManifestId(asccManifestId);
        return response;
    }

    @PostMapping(value = "/acc/{accManifestId:[\\d]+}/bcc/{bccpManifestId:[\\d]+}")
    public CcCreateResponse appendBcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId,
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId,
            @RequestParam(value = "pos", defaultValue = "-1") int pos) {

        BccManifestId bccManifestId = ccCommandService.createBcc(
                sessionService.asScoreUser(user),
                BccCreateRequest.builder(accManifestId, bccpManifestId).pos(pos).build());

        CcCreateResponse response = new CcCreateResponse();
        response.setManifestId(bccManifestId);
        return response;
    }

    @DeleteMapping(value = "/ascc/{asccManifestId:[\\d]+}")
    public void discardAscc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccManifestId") AsccManifestId asccManifestId) {

        ccCommandService.discard(sessionService.asScoreUser(user), asccManifestId);
    }

    @DeleteMapping(value = "/bcc/{bccManifestId:[\\d]+}")
    public void discardBcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bccManifestId") BccManifestId bccManifestId) {

        ccCommandService.discard(sessionService.asScoreUser(user), bccManifestId);
    }

    @PatchMapping(value = "/acc/{accManifestId:[\\d]+}/revise")
    public void reviseAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId) {

        ccCommandService.reviseAcc(sessionService.asScoreUser(user), accManifestId);
    }

    @PatchMapping(value = "/acc/{accManifestId:[\\d]+}/cancel")
    public void cancelAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId) {

        ccCommandService.cancelAcc(sessionService.asScoreUser(user), accManifestId);
    }

    @PostMapping(value = "/acc/{accManifestId:[\\d]+}/extension")
    public CcCreateResponse createAccExtensionComponent(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId) {
        AccManifestId manifestId = ccCommandService.createAccExtension(sessionService.asScoreUser(user), accManifestId);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @DeleteMapping(value = "/acc/{accManifestId:[\\d]+}/extension")
    public void purgeAccExtension(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId) {

        ccCommandService.purge(sessionService.asScoreUser(user), accManifestId);
    }

    @PostMapping(value = "/asccp")
    public CcCreateResponse createAsccp(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody AsccpCreateRequest request) {

        AsccpManifestId manifestId = ccCommandService.createAsccp(sessionService.asScoreUser(user), request);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @PatchMapping(value = "/asccp/{asccpManifestId:[\\d]+}/state")
    public ResponseEntity updateAsccpState(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId,
            @RequestParam("state") CcState state) {

        boolean updated = ccCommandService.updateState(sessionService.asScoreUser(user), asccpManifestId, state);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }

    @PatchMapping(value = "/asccp/{asccpManifestId:[\\d]+}/role-of-acc")
    public ResponseEntity updateAsccpRoleOfAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId,
            @RequestParam("accManifestId") AccManifestId accManifestId) {

        boolean updated = ccCommandService.updateAsccpRoleOfAcc(sessionService.asScoreUser(user), asccpManifestId, accManifestId);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }

    @DeleteMapping(value = "/asccp/{asccpManifestId:[\\d]+}")
    public void purgeAsccp(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId) {

        ccCommandService.purge(sessionService.asScoreUser(user), asccpManifestId);
    }

    @PatchMapping(value = "/asccp/{asccpManifestId:[\\d]+}/revise")
    public void reviseAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId) {

        ccCommandService.reviseAsccp(sessionService.asScoreUser(user), asccpManifestId);
    }

    @PatchMapping(value = "/asccp/{asccpManifestId:[\\d]+}/cancel")
    public void cancelAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId) {

        ccCommandService.cancelAsccp(sessionService.asScoreUser(user), asccpManifestId);
    }

    @PostMapping(value = "/oagis/bod")
    public CreateOagisBodResponse createOagisBod(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CreateOagisBodRequest request) {

        CreateOagisBodResponse response = ccCommandService.createOagisBod(sessionService.asScoreUser(user), request);
        return response;
    }

    @PostMapping(value = "/oagis/verb")
    public CreateOagisVerbResponse createBod(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CreateOagisVerbRequest request) {

        CreateOagisVerbResponse response = ccCommandService.createOagisVerb(sessionService.asScoreUser(user), request);
        return response;
    }

    @PostMapping(value = "/bccp")
    public CcCreateResponse createBccp(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody BccpCreateRequest request) {
        BccpManifestId manifestId = ccCommandService.createBccp(sessionService.asScoreUser(user), request);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @PatchMapping(value = "/bccp/{bccpManifestId:[\\d]+}/state")
    public void updateBccpState(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId,
            @RequestParam("state") CcState state) {

        ccCommandService.updateState(sessionService.asScoreUser(user), bccpManifestId, state);
    }

    @PatchMapping(value = "/bccp/{bccpManifestId:[\\d]+}/dt")
    public ResponseEntity updateBccpDt(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId,
            @RequestParam("dtManifestId") DtManifestId dtManifestId) {

        boolean updated = ccCommandService.updateBccpDt(sessionService.asScoreUser(user), bccpManifestId, dtManifestId);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }

    @DeleteMapping(value = "/bccp/{bccpManifestId:[\\d]+}")
    public void purgeBccp(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId) {

        ccCommandService.purge(sessionService.asScoreUser(user), bccpManifestId);
    }

    @DeleteMapping(value = "/dt-sc/{dtScManifestId:[\\d]+}")
    public void discardDtSc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtScManifestId") DtScManifestId dtScManifestId) {

        ccCommandService.discard(sessionService.asScoreUser(user), dtScManifestId, false);
    }

    @PatchMapping(value = "/bccp/{bccpManifestId:[\\d]+}/revise")
    public void reviseAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId) {

        ccCommandService.reviseBccp(sessionService.asScoreUser(user), bccpManifestId);
    }

    @PatchMapping(value = "/bccp/{bccpManifestId:[\\d]+}/cancel")
    public void cancelAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId) {

        ccCommandService.cancelBccp(sessionService.asScoreUser(user), bccpManifestId);
    }

    @PostMapping(value = "/dt")
    public CcCreateResponse createDt(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody DtCreateRequest request) {
        DtManifestId manifestId = ccCommandService.createDt(sessionService.asScoreUser(user), request);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @PostMapping(value = "/dt/{dtManifestId:[\\d]+}/dt-sc")
    public CcCreateResponse appendDtSc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtManifestId") DtManifestId ownerDtManifestId) {

        DtScManifestId manifestId = ccCommandService.createDtSc(
                sessionService.asScoreUser(user), ownerDtManifestId);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @PatchMapping(value = "/dt/{dtManifestId:[\\d]+}/state")
    public void updateDtState(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtManifestId") DtManifestId dtManifestId,
            @RequestParam("state") CcState state) {

        ccCommandService.updateState(sessionService.asScoreUser(user), dtManifestId, state);
    }

    @DeleteMapping(value = "/dt/{dtManifestId:[\\d]+}")
    public void purgeDtManifest(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtManifestId") DtManifestId dtManifestId) {

        ccCommandService.purge(sessionService.asScoreUser(user), dtManifestId);
    }

    @PatchMapping(value = "/dt/{dtManifestId:[\\d]+}/revise")
    public void reviseAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtManifestId") DtManifestId dtManifestId) {

        ccCommandService.reviseDt(sessionService.asScoreUser(user), dtManifestId);
    }

    @PatchMapping(value = "/dt/{dtManifestId:[\\d]+}/cancel")
    public void cancelAcc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtManifestId") DtManifestId dtManifestId) {

        ccCommandService.cancelDt(sessionService.asScoreUser(user), dtManifestId);
    }

    @PutMapping()
    public CcUpdateResponse updateCcNodeDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CcUpdateRequest request) {

        ScoreUser requester = sessionService.asScoreUser(user);

        CcUpdateResponse ccUpdateResponse = new CcUpdateResponse();
        ccUpdateResponse.setAccNodeResults(
                ccCommandService.updateAccList(requester, request.accUpdateRequestList()));
        ccUpdateResponse.setAsccNodeResults(
                ccCommandService.updateAsccList(requester, request.asccUpdateRequestList()));
        ccUpdateResponse.setAsccpNodeResults(
                ccCommandService.updateAsccpList(requester, request.asccpUpdateRequestList()));
        ccUpdateResponse.setBccNodeResults(
                ccCommandService.updateBccList(requester, request.bccUpdateRequestList()));
        ccUpdateResponse.setBccpNodeResults(
                ccCommandService.updateBccpList(requester, request.bccpUpdateRequestList()));
        ccUpdateResponse.setDtNodeResults(
                ccCommandService.updateDtList(requester, request.dtUpdateRequestList()));
        ccUpdateResponse.setDtScNodeResults(
                ccCommandService.updateDtScList(requester, request.dtScUpdateRequestList()));

        return ccUpdateResponse;
    }

    @PatchMapping(value = "/state")
    public void updateCcState(
            @AuthenticationPrincipal
            AuthenticatedPrincipal user,

            @RequestBody
            CcUpdateStateRequest request) {

        ScoreUser requester = sessionService.asScoreUser(user);
        request.accManifestIdList().forEach(accManifestId ->
                ccCommandService.updateState(requester, accManifestId, request.toState()));
        request.asccpManifestIdList().forEach(asccpManifestId ->
                ccCommandService.updateState(requester, asccpManifestId, request.toState()));
        request.bccpManifestIdList().forEach(bccpManifestId ->
                ccCommandService.updateState(requester, bccpManifestId, request.toState()));
        request.dtManifestIdList().forEach(dtManifestId ->
                ccCommandService.updateState(requester, dtManifestId, request.toState()));
    }

    @PatchMapping(value = "/mark-as-deleted")
    public void markAsDeleted(
            @AuthenticationPrincipal
            AuthenticatedPrincipal user,

            @RequestBody
            CcUpdateStateRequest request) {

        ScoreUser requester = sessionService.asScoreUser(user);
        request.accManifestIdList().forEach(accManifestId ->
                ccCommandService.updateState(requester, accManifestId, CcState.Deleted));
        request.asccpManifestIdList().forEach(asccpManifestId ->
                ccCommandService.updateState(requester, asccpManifestId, CcState.Deleted));
        request.bccpManifestIdList().forEach(bccpManifestId ->
                ccCommandService.updateState(requester, bccpManifestId, CcState.Deleted));
        request.dtManifestIdList().forEach(dtManifestId ->
                ccCommandService.updateState(requester, dtManifestId, CcState.Deleted));
    }

    @DeleteMapping()
    public void purge(
            @AuthenticationPrincipal
            AuthenticatedPrincipal user,

            @RequestBody
            CcUpdateStateRequest request) {

        ScoreUser requester = sessionService.asScoreUser(user);
        request.accManifestIdList().forEach(accManifestId -> {
            try {
                ccCommandService.purge(requester, accManifestId);
            } catch (Exception ignored) {
                logger.error("Error occurred while purging ACC " + accManifestId, ignored);
            }
        });
        request.asccpManifestIdList().forEach(asccpManifestId -> {
            try {
                ccCommandService.purge(requester, asccpManifestId);
            } catch (Exception ignored) {
                logger.error("Error occurred while purging ASCCP " + asccpManifestId, ignored);
            }
        });
        request.bccpManifestIdList().forEach(bccpManifestId -> {
            try {
                ccCommandService.purge(requester, bccpManifestId);
            } catch (Exception ignored) {
                logger.error("Error occurred while purging BCCP " + bccpManifestId, ignored);
            }
        });
        request.dtManifestIdList().forEach(dtManifestId -> {
            try {
                ccCommandService.purge(requester, dtManifestId);
            } catch (Exception ignored) {
                logger.error("Error occurred while purging DT " + dtManifestId, ignored);
            }
        });
    }

    @PatchMapping(value = "/restore")
    public void restore(
            @AuthenticationPrincipal
            AuthenticatedPrincipal user,

            @RequestBody
            CcUpdateStateRequest request) {

        ScoreUser requester = sessionService.asScoreUser(user);
        request.accManifestIdList().forEach(accManifestId ->
                ccCommandService.updateState(requester, accManifestId, CcState.WIP));
        request.asccpManifestIdList().forEach(asccpManifestId ->
                ccCommandService.updateState(requester, asccpManifestId, CcState.WIP));
        request.bccpManifestIdList().forEach(bccpManifestId ->
                ccCommandService.updateState(requester, bccpManifestId, CcState.WIP));
        request.dtManifestIdList().forEach(dtManifestId ->
                ccCommandService.updateState(requester, dtManifestId, CcState.WIP));
    }

    @Operation(summary = "Transfer ownership of an ACC",
            description = "Transfers ownership of the specified ACC to another user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ownership transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission"),
            @ApiResponse(responseCode = "404", description = "Record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/acc/{accManifestId:[\\d]+}/transfer")
    public ResponseEntity<Void> transferOwnership(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the ACC manifest to transfer", required = true)
            @PathVariable("accManifestId") AccManifestId accManifestId,

            @RequestBody CcTransferOwnershipRequest request) {

        ccCommandService.transferOwnership(sessionService.asScoreUser(user),
                sessionService.getScoreUserByUsername(request.targetLoginId()),
                accManifestId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Transfer ownership of an ASCCP",
            description = "Transfers ownership of the specified ASCCP to another user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ownership transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission"),
            @ApiResponse(responseCode = "404", description = "Record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/asccp/{asccpManifestId:[\\d]+}/transfer")
    public ResponseEntity<Void> transferOwnership(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the ASCCP manifest to transfer", required = true)
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId,

            @RequestBody CcTransferOwnershipRequest request) {

        ccCommandService.transferOwnership(sessionService.asScoreUser(user),
                sessionService.getScoreUserByUsername(request.targetLoginId()),
                asccpManifestId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Transfer ownership of an BCCP",
            description = "Transfers ownership of the specified BCCP to another user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ownership transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission"),
            @ApiResponse(responseCode = "404", description = "Record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/bccp/{bccpManifestId:[\\d]+}/transfer")
    public ResponseEntity<Void> transferOwnership(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the BCCP manifest to transfer", required = true)
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId,

            @RequestBody CcTransferOwnershipRequest request) {

        ccCommandService.transferOwnership(sessionService.asScoreUser(user),
                sessionService.getScoreUserByUsername(request.targetLoginId()),
                bccpManifestId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Transfer ownership of an DT",
            description = "Transfers ownership of the specified DT to another user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ownership transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission"),
            @ApiResponse(responseCode = "404", description = "Record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/dt/{dtManifestId:[\\d]+}/transfer")
    public ResponseEntity<Void> transferOwnership(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the DT manifest to transfer", required = true)
            @PathVariable("dtManifestId") DtManifestId dtManifestId,

            @RequestBody CcTransferOwnershipRequest request) {

        ccCommandService.transferOwnership(sessionService.asScoreUser(user),
                sessionService.getScoreUserByUsername(request.targetLoginId()),
                dtManifestId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/transfer")
    public ResponseEntity<Void> transferOwnership(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CcTransferOwnershipListRequest request) {

        request.accManifestIdList().forEach(accManifestId -> {
            transferOwnership(user, accManifestId, new CcTransferOwnershipRequest(request.targetLoginId()));
        });
        request.asccpManifestIdList().forEach(asccpManifestId -> {
            transferOwnership(user, asccpManifestId, new CcTransferOwnershipRequest(request.targetLoginId()));
        });
        request.bccpManifestIdList().forEach(bccpManifestId -> {
            transferOwnership(user, bccpManifestId, new CcTransferOwnershipRequest(request.targetLoginId()));
        });
        request.dtManifestIdList().forEach(dtManifestId -> {
            transferOwnership(user, dtManifestId, new CcTransferOwnershipRequest(request.targetLoginId()));
        });

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/ascc/refactor")
    public void refactorAssociation(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam("targetManifestId") AsccManifestId targetManifestId,
            @RequestParam("destinationManifestId") AccManifestId destinationManifestId) {

        ccCommandService.refactorAscc(
                sessionService.asScoreUser(user),
                targetManifestId, destinationManifestId);
    }

    @PostMapping(value = "/bcc/refactor")
    public void refactorAssociation(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam("targetManifestId") BccManifestId targetManifestId,
            @RequestParam("destinationManifestId") AccManifestId destinationManifestId) {

        ccCommandService.refactorBcc(
                sessionService.asScoreUser(user),
                targetManifestId, destinationManifestId);
    }

    @PostMapping(value = "/acc/{accManifestId:[\\d]+}/ungroup")
    public void ungroup(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId,
            @RequestParam("asccManifestId") AsccManifestId asccManifestId,
            @RequestParam(value = "pos", defaultValue = "-1") int pos) {

        ccCommandService.ungroup(sessionService.asScoreUser(user),
                accManifestId, asccManifestId, pos);
    }

}
