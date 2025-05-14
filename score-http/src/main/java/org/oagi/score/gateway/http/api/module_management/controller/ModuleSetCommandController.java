package org.oagi.score.gateway.http.api.module_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.module_management.controller.payload.*;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.service.ModuleSetCommandService;
import org.oagi.score.gateway.http.api.module_management.service.ModuleSetModuleCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Module Set - Commands", description = "API for creating, updating, and deleting module sets")
@RequestMapping("/module-sets")
public class ModuleSetCommandController {

    @Autowired
    private ModuleSetCommandService moduleSetCommandService;

    @Autowired
    private ModuleSetModuleCommandService moduleSetModuleCommandService;

    @Autowired
    private SessionService sessionService;

    @PostMapping()
    public CreateModuleSetResponse createModuleSet(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CreateModuleSetRequest request) {

        ModuleSetId newModuleSetId = moduleSetCommandService.create(
                sessionService.asScoreUser(user), request);
        return new CreateModuleSetResponse(newModuleSetId, "success", "");
    }

    @PutMapping(value = "/{moduleSetId:[\\d]+}")
    public void updateModuleSet(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetId") ModuleSetId moduleSetId,
            @RequestBody UpdateModuleSetRequest request) {

        moduleSetCommandService.update(
                sessionService.asScoreUser(user), request.withModuleSetId(moduleSetId));
    }

    @DeleteMapping(value = "/{moduleSetId:[\\d]+}")
    public void discardModuleSet(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetId") ModuleSetId moduleSetId) {

        moduleSetCommandService.discard(sessionService.asScoreUser(user), moduleSetId);
    }

    @PostMapping(value = "/{moduleSetId:[\\d]+}/modules")
    public CreateModuleSetModuleResponse addModuleSetModule(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetId") ModuleSetId moduleSetId,
            @RequestBody CreateModuleSetModuleRequest request) {

        ModuleId newModuleId = moduleSetModuleCommandService.create(
                sessionService.asScoreUser(user), request.withModuleSetId(moduleSetId));
        return new CreateModuleSetModuleResponse(newModuleId, "success", "");
    }

    @PutMapping(value = "/{moduleSetId:[\\d]+}/modules/{moduleId:[\\d]+}")
    public void updateModuleSetModule(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetId") ModuleSetId moduleSetId,
            @PathVariable("moduleId") ModuleId moduleId,
            @RequestBody UpdateModuleSetModuleRequest request) {

        moduleSetModuleCommandService.update(
                sessionService.asScoreUser(user), request.withModuleIdAndModuleSetId(moduleId, moduleSetId));
    }

    @DeleteMapping(value = "/{moduleSetId:[\\d]+}/modules/{moduleId:[\\d]+}")
    public void discardModuleSetModule(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetId") ModuleSetId moduleSetId,
            @PathVariable("moduleId") ModuleId moduleId) {

        moduleSetModuleCommandService.discard(
                sessionService.asScoreUser(user), moduleId, moduleSetId);
    }

    @PostMapping(value = "/{moduleSetId:[\\d]+}/modules/{moduleId:[\\d]+}/copy")
    public void copyModule(@AuthenticationPrincipal AuthenticatedPrincipal user,
                           @PathVariable("moduleSetId") ModuleSetId moduleSetId,
                           @PathVariable("moduleId") ModuleId moduleId,
                           @RequestBody CopyModuleSetModuleRequest request) {

        moduleSetModuleCommandService.copyModule(
                sessionService.asScoreUser(user), request.withModuleSetIdAndParentModuleId(moduleSetId, moduleId));
    }

}
