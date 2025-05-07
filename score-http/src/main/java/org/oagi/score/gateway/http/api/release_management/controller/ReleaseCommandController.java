package org.oagi.score.gateway.http.api.release_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.release_management.controller.payload.*;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.Utility.separate;

@RestController
@Tag(name = "Release - Commands", description = "API for creating, updating, and deleting releases")
@RequestMapping("/releases")
public class ReleaseCommandController {

    @Autowired
    private ReleaseCommandService releaseCommandService;

    @Autowired
    private SessionService sessionService;

    @Operation(summary = "Create a new release")
    @PostMapping()
    public CreateReleaseResponse createRelease(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                               @RequestBody CreateReleaseRequest request) {

        ReleaseId newReleaseId = releaseCommandService.create(sessionService.asScoreUser(user), request);
        return new CreateReleaseResponse(newReleaseId, "success", "");
    }

    @Operation(summary = "Update an existing release")
    @PutMapping(value = "/{releaseId:[\\d]+}")
    public void updateRelease(@AuthenticationPrincipal AuthenticatedPrincipal user,
                              @PathVariable("releaseId") ReleaseId releaseId,
                              @RequestBody UpdateReleaseRequest request) {

        releaseCommandService.update(sessionService.asScoreUser(user),
                request.withReleaseId(releaseId));
    }

    @Operation(summary = "Delete a release by ID")
    @DeleteMapping(value = "/{releaseId:[\\d]+}")
    public void discard(@AuthenticationPrincipal AuthenticatedPrincipal user,
                        @PathVariable("releaseId") ReleaseId releaseId) {

        releaseCommandService.discard(sessionService.asScoreUser(user), releaseId);
    }

    @Operation(summary = "Delete multiple releases by IDs")
    @DeleteMapping()
    public void discard(@AuthenticationPrincipal AuthenticatedPrincipal user,
                        @RequestParam(name = "releaseIds") String releaseIds) {

        releaseCommandService.discard(sessionService.asScoreUser(user),
                separate(releaseIds).map(e -> new ReleaseId(new BigInteger(e))).collect(toSet()));
    }

    @PostMapping(value = "/{releaseId:[\\d]+}/state")
    public void transitState(@AuthenticationPrincipal AuthenticatedPrincipal user,
                             @PathVariable("releaseId") ReleaseId releaseId,
                             @RequestBody TransitStateRequest request) {
        request.setReleaseId(releaseId);
        releaseCommandService.transitState(sessionService.asScoreUser(user), request);
    }

    @PostMapping(value = "/{releaseId:[\\d]+}/validate")
    public ReleaseValidationResponse validate(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                              @PathVariable("releaseId") ReleaseId releaseId,
                                              @RequestBody ReleaseValidationRequest request) {
        request.setReleaseId(releaseId);
        return releaseCommandService.validate(sessionService.asScoreUser(user), request);
    }

    @PostMapping(value = "/{releaseId:[\\d]+}/draft")
    public ReleaseValidationResponse createDraft(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                 @PathVariable("releaseId") ReleaseId releaseId,
                                                 @RequestBody ReleaseValidationRequest request) {
        request.setReleaseId(releaseId);
        return releaseCommandService.createDraft(sessionService.asScoreUser(user), request);
    }

}
