package org.oagi.score.gateway.http.api.passkey_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeySummary;
import org.oagi.score.gateway.http.api.passkey_management.service.PasskeyQueryService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Passkeys - Queries", description = "API for listing registered passkeys")
public class PasskeyQueryController {

    @Autowired
    private PasskeyQueryService service;

    @Autowired
    private SessionService sessionService;

    /**
     * Lists the signed-in user's own passkeys.
     */
    @GetMapping(value = "/passkeys", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PasskeySummary> getMyPasskeys(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return service.getMyPasskeys(sessionService.asScoreUser(user));
    }

}
