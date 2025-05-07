package org.oagi.score.gateway.http.api.account_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.account_management.controller.payload.AccountCreateRequest;
import org.oagi.score.gateway.http.api.account_management.controller.payload.UpdateAccountRequest;
import org.oagi.score.gateway.http.api.account_management.controller.payload.UpdateEmailRequest;
import org.oagi.score.gateway.http.api.account_management.controller.payload.UpdatePasswordRequest;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.service.AccountCommandService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "Account - Commands", description = "API for creating, updating, and deleting accounts")
@RequestMapping("/accounts")
public class AccountCommandController {

    @Autowired
    private AccountCommandService accountCommandService;

    @Autowired
    private SessionService sessionService;

    @PostMapping()
    public ResponseEntity create(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody AccountCreateRequest request) {
        accountCommandService.create(sessionService.asScoreUser(user), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping()
    public ResponseEntity update(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody UpdateAccountRequest request) {

        ScoreUser requester = sessionService.asScoreUser(user);
        return update(user, requester.userId(), request);
    }

    @PutMapping(value = "/{appUserId:[\\d]+}")
    public ResponseEntity update(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("appUserId") UserId appUserId,
            @RequestBody UpdateAccountRequest request) {

        accountCommandService.update(sessionService.asScoreUser(user), request.withUserId(appUserId));
        return ResponseEntity.accepted().build();
    }

    @PutMapping(value = "/password")
    public ResponseEntity updatePassword(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody UpdatePasswordRequest request) {

        accountCommandService.updatePassword(sessionService.asScoreUser(user), request);
        return ResponseEntity.accepted().build();
    }

    @PutMapping(value = "/email")
    public ResponseEntity updateEmail(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody UpdateEmailRequest request) {

        accountCommandService.updateEmail(sessionService.asScoreUser(user), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/email-validation")
    public ResponseEntity verifyEmailValidation(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody Map<String, String> params) {
        String q = params.get("q");
        accountCommandService.verifyEmailValidation(sessionService.asScoreUser(user), q);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/resend-email-validation-request")
    public ResponseEntity resendEmailValidationRequest(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody UpdateEmailRequest request) {
        accountCommandService.sendEmailValidationRequest(sessionService.asScoreUser(user), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{appUserId:[\\d]+}/delink")
    public ResponseEntity delinkUser(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("appUserId") UserId appUserId) {
        accountCommandService.delinkOAuth2User(sessionService.asScoreUser(user), appUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{appUserId:[\\d]+}")
    public ResponseEntity discard(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("appUserId") UserId appUserId) {
        accountCommandService.discard(sessionService.asScoreUser(user), appUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{appUserId:[\\d]+}/enable")
    public ResponseEntity enable(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("appUserId") UserId userId) {
        accountCommandService.setEnable(sessionService.asScoreUser(user), userId, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{appUserId:[\\d]+}/disable")
    public ResponseEntity disable(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("appUserId") UserId userId) {
        accountCommandService.setEnable(sessionService.asScoreUser(user), userId, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{appUserId:[\\d]+}/transfer")
    public ResponseEntity transferOwnership(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("appUserId") UserId userId) {
        accountCommandService.transferOwnership(sessionService.asScoreUser(user), userId);
        return ResponseEntity.accepted().build();
    }

}