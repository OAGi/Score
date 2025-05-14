package org.oagi.score.gateway.http.api.info_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.info_management.model.WebPageInfoRecord;
import org.oagi.score.gateway.http.api.info_management.service.WebPageInfoCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Information - Commands", description = "API for creating, updating, and deleting information")
@RequestMapping("/info")
public class InfoCommandController {

    @Autowired
    private WebPageInfoCommandService webPageInfoService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping(value = "/webpages")
    public void updateWebPageInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody WebPageInfoRecord webPageInfo) {
        webPageInfoService.updateWebPageInfo(sessionService.asScoreUser(user), webPageInfo);
        simpMessagingTemplate.convertAndSend("/topic/webpage/info", webPageInfo);
    }

}
