package org.oagi.score.gateway.http.api.bie_management.controller;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieTransferOwnershipListRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieUpdateStateListRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieEvent;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.service.BieService;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.mail.controller.payload.SendMailRequest;
import org.oagi.score.gateway.http.api.mail.service.MailService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class BieListController {

    @Autowired
    private BieService bieService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MailService mailService;

    @RequestMapping(value = "/profile_bie/business_ctx_from_abie/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BusinessContextSummaryRecord findBizCtxFromAbieId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("id") AbieId abieId) {

        return bieService.findBizCtxByAbieId(sessionService.asScoreUser(user), abieId);
    }

    @RequestMapping(value = "/profile_bie/{id:[\\d]+}/transfer_ownership", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity transferOwnership(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @PathVariable("id") TopLevelAsbiepId topLevelAsbiepId,
                                            @RequestParam(value = "sendNotification", required = false) Boolean sendNotification,
                                            @RequestBody Map<String, Object> requestBody) {
        String targetLoginId = (String) requestBody.get("targetLoginId");
        bieService.transferOwnership(sessionService.asScoreUser(user), topLevelAsbiepId, targetLoginId);

        BieEvent event = new BieEvent();
        event.setAction("UpdateOwnership");
        event.setTopLevelAsbiepId(topLevelAsbiepId);
        event.addProperty("actor", user.getName());
        event.addProperty("target", targetLoginId);
        event.addProperty("timestamp", LocalDateTime.now());
        bieService.fireBieEvent(event);

        if (sendNotification != null && sendNotification) {
            SendMailRequest sendMailRequest = new SendMailRequest();
            sendMailRequest.setRecipient(sessionService.getScoreUserByUsername(targetLoginId));
            sendMailRequest.setTemplateName("bie-ownership-transfer-acceptance");
            sendMailRequest.setParameters((Map<String, Object>) requestBody.get("parameters"));
            mailService.sendMail(sessionService.asScoreUser(user), sendMailRequest);
        }

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/bie_list/state/multiple",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateCcState(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                        @RequestBody BieUpdateStateListRequest request) {
        bieService.updateStateBieList(sessionService.asScoreUser(user), request);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/bie_list/transfer_ownership/multiple",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity transferOwnership(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @RequestBody BieTransferOwnershipListRequest request) {
        bieService.transferOwnershipList(sessionService.asScoreUser(user), request);

        request.getTopLevelAsbiepIds().forEach(topLevelAsbiepId -> {
            BieEvent event = new BieEvent();
            event.setAction("UpdateOwnership");
            event.setTopLevelAsbiepId(topLevelAsbiepId);
            event.addProperty("actor", user.getName());
            event.addProperty("target", request.getTargetLoginId());
            event.addProperty("timestamp", LocalDateTime.now());
            bieService.fireBieEvent(event);
        });

        return ResponseEntity.noContent().build();
    }

}
