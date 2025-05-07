package org.oagi.score.gateway.http.api.module_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.module_management.controller.payload.AssignCCToModuleRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.CreateModuleSetReleaseRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.CreateModuleSetReleaseResponse;
import org.oagi.score.gateway.http.api.module_management.controller.payload.UpdateModuleSetReleaseRequest;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.module_management.service.ModuleSetReleaseCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Module Set Release - Commands", description = "API for creating, updating, and deleting module set releases")
@RequestMapping("/module-set-releases")
public class ModuleSetReleaseCommandController {

    @Autowired
    private ModuleSetReleaseCommandService moduleSetReleaseCommandService;

    @Autowired
    private SessionService sessionService;

    @PostMapping()
    public CreateModuleSetReleaseResponse createModuleSetRelease(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CreateModuleSetReleaseRequest request) {

        ModuleSetReleaseId newModuleSetReleaseId = moduleSetReleaseCommandService.create(
                sessionService.asScoreUser(user), request);
        return new CreateModuleSetReleaseResponse(newModuleSetReleaseId, "success", "");
    }

    @PutMapping(value = "/{moduleSetReleaseId:[\\d]+}")
    public void updateModuleSetRelease(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetReleaseId") ModuleSetReleaseId moduleSetReleaseId,
            @RequestBody UpdateModuleSetReleaseRequest request) {

        moduleSetReleaseCommandService.update(
                sessionService.asScoreUser(user), request.withModuleSetReleaseId(moduleSetReleaseId));
    }

    @DeleteMapping(value = "/{moduleSetReleaseId:[\\d]+}")
    public void discardModuleSetRelease(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetReleaseId") ModuleSetReleaseId moduleSetReleaseId) {

        moduleSetReleaseCommandService.discard(sessionService.asScoreUser(user), moduleSetReleaseId);
    }

    @PostMapping(value = "/{moduleSetReleaseId:[\\d]+}/assign")
    public void assignCCs(@AuthenticationPrincipal AuthenticatedPrincipal user,
                          @PathVariable("moduleSetReleaseId") ModuleSetReleaseId moduleSetReleaseId,
                          @RequestBody AssignCCToModuleRequest assignCCToModule) {

        moduleSetReleaseCommandService.assignCc(
                sessionService.asScoreUser(user), assignCCToModule.withModuleSetReleaseId(moduleSetReleaseId));
    }

    @PostMapping(value = "/{moduleSetReleaseId:[\\d]+}/unassign")
    public void unassignCCs(@AuthenticationPrincipal AuthenticatedPrincipal user,
                            @PathVariable("moduleSetReleaseId") ModuleSetReleaseId moduleSetReleaseId,
                            @RequestBody AssignCCToModuleRequest assignCCToModule) {

        moduleSetReleaseCommandService.unassignCc(
                sessionService.asScoreUser(user), assignCCToModule.withModuleSetReleaseId(moduleSetReleaseId));
    }

}
