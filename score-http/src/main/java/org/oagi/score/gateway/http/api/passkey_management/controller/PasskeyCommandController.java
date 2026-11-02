package org.oagi.score.gateway.http.api.passkey_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.controller.payload.PasskeyRenameRequest;
import org.oagi.score.gateway.http.api.passkey_management.service.PasskeyCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Passkeys - Commands", description = "API for renaming and removing passkeys")
public class PasskeyCommandController {

    @Autowired
    private PasskeyCommandService service;

    @Autowired
    private SessionService sessionService;

    @PatchMapping(value = "/passkeys/{id}")
    public ResponseEntity renamePasskey(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                        @PathVariable("id") String id,
                                        @RequestBody PasskeyRenameRequest request) {
        service.renamePasskey(sessionService.asScoreUser(user), id, request.label());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/passkeys/{id}")
    public ResponseEntity deletePasskey(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                        @PathVariable("id") String id) {
        service.deleteMyPasskey(sessionService.asScoreUser(user), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Administrator: removes all of a user's passkeys, optionally ending the user's sessions too.
     */
    @DeleteMapping(value = "/passkey-admin/users/{appUserId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Integer> deletePasskeysOfUser(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("appUserId") UserId appUserId,
            @RequestParam(name = "invalidateSessions", defaultValue = "false") boolean invalidateSessions) {
        return Map.of("removed",
                service.deletePasskeysOfUser(sessionService.asScoreUser(user), appUserId, invalidateSessions));
    }

    /**
     * Administrator: removes every passkey registered for an RP ID other than the configured one.
     */
    @DeleteMapping(value = "/passkey-admin/stale", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Integer> deleteStalePasskeys(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return Map.of("removed", service.deleteStalePasskeys(sessionService.asScoreUser(user)));
    }

}
