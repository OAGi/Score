package org.oagi.score.gateway.http.api.bie_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieCopyRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieCreateRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieCreateResponse;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.DeleteBieListRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.service.BieCommandService;
import org.oagi.score.gateway.http.api.bie_management.service.BieCopyService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Business Information Entity - Commands", description = "API for creating, updating, and deleting BIEs")
@RequestMapping("/bies")
public class BieCommandController {

    @Autowired
    private BieCommandService bieCommandService;

    @Autowired
    private BieCopyService bieCopyService;

    @Autowired
    private SessionService sessionService;

    @PostMapping()
    public BieCreateResponse create(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody BieCreateRequest bieCreateRequest) {

        TopLevelAsbiepId topLevelAsbiepId = bieCommandService.createBie(sessionService.asScoreUser(user), bieCreateRequest);
        return new BieCreateResponse(topLevelAsbiepId);
    }

    @PostMapping(value = "/{topLevelAsbiepId:[\\d]+}/copy")
    public ResponseEntity copy(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
            @RequestBody BieCopyRequest request) {

        bieCopyService.copyBie(sessionService.asScoreUser(user), topLevelAsbiepId, request.bizCtxIdList());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping()
    public ResponseEntity discardBieList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody DeleteBieListRequest request) {

        List<TopLevelAsbiepId> topLevelAsbiepIdList = request.topLevelAsbiepIdList();
        bieCommandService.discardBieList(sessionService.asScoreUser(user), topLevelAsbiepIdList);
        return ResponseEntity.noContent().build();
    }

}
