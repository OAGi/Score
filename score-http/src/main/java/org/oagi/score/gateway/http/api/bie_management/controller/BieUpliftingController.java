package org.oagi.score.gateway.http.api.bie_management.controller;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.*;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.service.BieUpliftingService;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class BieUpliftingController {

    @Autowired
    private BieUpliftingService upliftingService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/uplifting/target", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AsccpSummaryRecord findTargetAsccpManifest(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                      @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                                                      @RequestParam("targetReleaseId") ReleaseId targetReleaseId) {

        return upliftingService.findTargetAsccpManifest(sessionService.asScoreUser(user), topLevelAsbiepId, targetReleaseId);
    }


    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/uplifting", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AnalysisBieUpliftingResponse analysisBieUplifting(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                             @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                                                             @RequestParam("targetReleaseId") ReleaseId targetReleaseId) {

        return upliftingService.analysisBieUplifting(sessionService.asScoreUser(user), topLevelAsbiepId, targetReleaseId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/uplifting", method = RequestMethod.POST)
    public UpliftBieResponse upliftBie(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                       @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                                       @RequestBody UpliftBieRequest request) {

        request.setRequester(sessionService.asScoreUser(user));
        request.setTopLevelAsbiepId(topLevelAsbiepId);

        return upliftingService.upliftBie(sessionService.asScoreUser(user), request);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/uplifting/{targetReleaseId:[\\d]+}/valid", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UpliftValidationResponse validateUplift(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                   @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                                                   @PathVariable("targetReleaseId") ReleaseId targetReleaseId,
                                                   @RequestBody UpliftValidationRequest request) {
        request.setRequester(sessionService.asScoreUser(user));
        request.setTopLevelAsbiepId(topLevelAsbiepId);
        request.setTargetReleaseId(targetReleaseId);
        return upliftingService.validateBieUplifting(sessionService.asScoreUser(user), request);
    }

}
