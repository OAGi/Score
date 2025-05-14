package org.oagi.score.gateway.http.api.bie_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.*;
import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.service.BiePackageCommandService;
import org.oagi.score.gateway.http.api.mail.controller.payload.SendMailRequest;
import org.oagi.score.gateway.http.api.mail.service.MailService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@Tag(name = "BIE Package - Commands", description = "API for creating, updating, and deleting BIE packages")
@RequestMapping("/bie-packages")
public class BiePackageCommandController {

    @Autowired
    private BiePackageCommandService biePackageCommandService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MailService mailService;

    @PostMapping()
    public CreateBiePackageResponse createBiePackage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CreateBiePackageRequest request) {

        BiePackageId newBiePackageId = biePackageCommandService.create(
                sessionService.asScoreUser(user), request);
        return new CreateBiePackageResponse(newBiePackageId, "success", "");
    }

    @PutMapping(value = "/{biePackageId:[\\d]+}")
    public void updateBiePackage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("biePackageId") BiePackageId biePackageId,
            @RequestBody UpdateBiePackageRequest request) {

        biePackageCommandService.update(
                sessionService.asScoreUser(user), request.withBiePackageId(biePackageId));
    }

    @PostMapping(value = "/bie-packages/copy")
    public ResponseEntity copy(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CopyBiePackageRequest request) {

        biePackageCommandService.copy(sessionService.asScoreUser(user), request.biePackageIdList());

        return ResponseEntity.accepted().build();
    }

    @DeleteMapping(value = "/{biePackageId:[\\d]+}")
    public void discardBiePackage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("biePackageId") BiePackageId biePackageId) throws ScoreDataAccessException {

        discardBiePackage(user, new DiscardBiePackageRequest(Arrays.asList(biePackageId)));
    }

    @DeleteMapping()
    public void discardBiePackage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody DiscardBiePackageRequest request) throws ScoreDataAccessException {

        biePackageCommandService.discard(sessionService.asScoreUser(user), request);
    }

    @PatchMapping(value = "/{biePackageId:[\\d]+}/transfer")
    public void transferOwnership(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("biePackageId") BiePackageId biePackageId,
            @RequestBody BiePackageTransferOwnershipRequest request) {

        ScoreUser targetUser = sessionService.getScoreUserByUsername(request.targetLoginId());
        if (targetUser == null) {
            throw new IllegalArgumentException("Not found a target user.");
        }

        boolean transferred = biePackageCommandService.transferOwnership(
                sessionService.asScoreUser(user), biePackageId, targetUser);
        if (transferred && (request.sendNotification() != null && request.sendNotification())) {
            SendMailRequest sendMailRequest = new SendMailRequest();
            sendMailRequest.setRecipient(sessionService.getScoreUserByUsername(request.targetLoginId()));
            sendMailRequest.setTemplateName("bie-package-ownership-transfer-acceptance");
            sendMailRequest.setParameters(request.mailParameters());
            mailService.sendMail(sessionService.asScoreUser(user), sendMailRequest);
        }
    }

    @PostMapping(value = "/{biePackageId:[\\d]+}/bies")
    public ResponseEntity addBieToBiePackage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("biePackageId") BiePackageId biePackageId,
            @RequestBody AddBieToBiePackageRequest request) throws ScoreDataAccessException {

        biePackageCommandService.addBieToBiePackage(
                sessionService.asScoreUser(user), biePackageId, request.topLevelAsbiepIdList()
        );

        return ResponseEntity.accepted().build();
    }

    @DeleteMapping(value = "/{biePackageId:[\\d]+}/bies/{topLevelAsbiepId:\\d+}")
    public ResponseEntity deleteBieInBiePackage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("biePackageId") BiePackageId biePackageId,
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId) throws ScoreDataAccessException {

        biePackageCommandService.deleteBieInBiePackage(
                sessionService.asScoreUser(user), biePackageId, Arrays.asList(topLevelAsbiepId)
        );

        return ResponseEntity.noContent().build();

    }

    @DeleteMapping(value = "/{biePackageId:[\\d]+}/bies")
    public ResponseEntity deleteBieInBiePackage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("biePackageId") BiePackageId biePackageId,
            @RequestBody DeleteBieInBiePackageRequest request) throws ScoreDataAccessException {

        biePackageCommandService.deleteBieInBiePackage(
                sessionService.asScoreUser(user), biePackageId, request.topLevelAsbiepIdList()
        );

        return ResponseEntity.noContent().build();
    }

}
