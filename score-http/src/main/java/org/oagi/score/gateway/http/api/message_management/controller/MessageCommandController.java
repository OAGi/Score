package org.oagi.score.gateway.http.api.message_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.message_management.controller.payload.DiscardMessageRequest;
import org.oagi.score.gateway.http.api.message_management.model.MessageId;
import org.oagi.score.gateway.http.api.message_management.service.MessageCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Message - Commands", description = "API for creating, updating, and deleting messages")
@RequestMapping("/messages")
public class MessageCommandController {

    @Autowired
    private MessageCommandService messageCommandService;

    @Autowired
    private SessionService sessionService;

    @DeleteMapping(value = "/{messageId:[\\d]+}")
    public void discardMessage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("messageId") MessageId messageId) {

        messageCommandService.discard(sessionService.asScoreUser(user), messageId);
    }

    @DeleteMapping()
    public void discardMessages(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody DiscardMessageRequest request) {

        messageCommandService.discard(sessionService.asScoreUser(user), request.messageIdList());
    }
}
