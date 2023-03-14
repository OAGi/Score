package org.oagi.score.gateway.http.api.bie_management.controller;

import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.service.bie.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
public class BieUpliftingController {

    @Autowired
    private BieUpliftingService upliftingService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/uplifting/target", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FindTargetAsccpManifestResponse findTargetAsccpManifest(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                                   @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                                                   @RequestParam("targetReleaseId") BigInteger targetReleaseId) {

        FindTargetAsccpManifestRequest request = new FindTargetAsccpManifestRequest();
        request.setRequester(sessionService.asScoreUser(user));
        request.setTopLevelAsbiepId(topLevelAsbiepId);
        request.setTargetReleaseId(targetReleaseId);

        return upliftingService.findTargetAsccpManifest(request);
    }


    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/uplifting", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AnalysisBieUpliftingResponse analysisBieUplifting(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                             @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                                             @RequestParam("targetReleaseId") BigInteger targetReleaseId) {

        AnalysisBieUpliftingRequest request = new AnalysisBieUpliftingRequest();
        request.setRequester(sessionService.asScoreUser(user));
        request.setTopLevelAsbiepId(topLevelAsbiepId);
        request.setTargetReleaseId(targetReleaseId);

        return upliftingService.analysisBieUplifting(request);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/uplifting", method = RequestMethod.POST)
    public UpliftBieResponse upliftBie(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                       @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                       @RequestBody UpliftBieRequest request) {

        request.setRequester(sessionService.asScoreUser(user));
        request.setTopLevelAsbiepId(topLevelAsbiepId);

        return upliftingService.upliftBie(request);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/uplifting/{targetReleaseId:[\\d]+}/valid", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UpliftValidationResponse validateUplift(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                   @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                                   @PathVariable("targetReleaseId") BigInteger targetReleaseId,
                                                   @RequestBody UpliftValidationRequest request) {
        request.setRequester(sessionService.asScoreUser(user));
        request.setTopLevelAsbiepId(topLevelAsbiepId);
        request.setTargetReleaseId(targetReleaseId);
        return upliftingService.validateBieUplifting(request);
    }

}
