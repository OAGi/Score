package org.oagi.score.gateway.http.api.mail.controller;

import org.oagi.score.gateway.http.api.mail.data.SendMailRequest;
import org.oagi.score.gateway.http.api.mail.service.MailService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
public class MailController {

    @Autowired
    private MailService mailService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/mail/{template_name}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity sendMail(@AuthenticationPrincipal AuthenticatedPrincipal requester,
                                   @PathVariable("template_name") String templateName,
                                   @RequestParam("recipient") Object recipient,
                                   @RequestBody SendMailRequest sendMailRequest) {

        sendMailRequest.setTemplateName(templateName);
        try {
            BigInteger recipientId = new BigInteger(recipient.toString());
            sendMailRequest.setRecipient(sessionService.getScoreUserByUserId(recipientId));
        } catch (NumberFormatException e) {
            sendMailRequest.setRecipient(sessionService.getScoreUserByUsername(recipient.toString()));
        }

        mailService.sendMail(sessionService.asScoreUser(requester), sendMailRequest);

        return ResponseEntity.accepted().build();
    }
}
