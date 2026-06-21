package org.oagi.score.gateway.http.api.library_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.library_management.controller.payload.CreateLibraryRequest;
import org.oagi.score.gateway.http.api.library_management.controller.payload.CreateLibraryResponse;
import org.oagi.score.gateway.http.api.library_management.controller.payload.DiscardLibraryCheckResponse;
import org.oagi.score.gateway.http.api.library_management.controller.payload.UpdateLibraryReleaseDependenciesRequest;
import org.oagi.score.gateway.http.api.library_management.controller.payload.UpdateLibraryRequest;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.service.LibraryCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Library - Commands", description = "API for creating, updating, and deleting libraries")
@RequestMapping("/libraries")
public class LibraryCommandController {

    @Autowired
    private LibraryCommandService libraryCommandService;

    @Autowired
    private SessionService sessionService;

    @PostMapping()
    public CreateLibraryResponse createLibrary(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CreateLibraryRequest request) {

        LibraryId newLibraryId = libraryCommandService.create(sessionService.asScoreUser(user), request);
        return new CreateLibraryResponse(newLibraryId, "success", "");
    }

    @PutMapping(value = "/{libraryId:[\\d]+}")
    public void updateLibrary(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("libraryId") LibraryId libraryId,
            @RequestBody UpdateLibraryRequest request) {

        libraryCommandService.update(sessionService.asScoreUser(user), request.withLibraryId(libraryId));
    }

    @GetMapping(value = "/{libraryId:[\\d]+}/discard-check")
    public DiscardLibraryCheckResponse checkDiscardLibrary(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("libraryId") LibraryId libraryId) {

        return libraryCommandService.checkDiscard(sessionService.asScoreUser(user), libraryId);
    }

    @PutMapping(value = "/{libraryId:[\\d]+}/release-dependencies")
    public void updateLibraryReleaseDependencies(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("libraryId") LibraryId libraryId,
            @RequestBody UpdateLibraryReleaseDependenciesRequest request) {

        libraryCommandService.updateReleaseDependencies(sessionService.asScoreUser(user), libraryId, request);
    }

    @DeleteMapping(value = "/{libraryId:[\\d]+}")
    public void discardLibrary(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("libraryId") LibraryId libraryId) {

        libraryCommandService.discard(sessionService.asScoreUser(user), libraryId);
    }

}
