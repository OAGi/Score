package org.oagi.score.gateway.http.api.bie_management.controller;

import org.oagi.score.gateway.http.api.bie_management.service.BiePackageService;
import org.oagi.score.gateway.http.api.mail.service.MailService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BiePackageController {

    @Autowired
    private BiePackageService service;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MailService mailService;
//
//    @RequestMapping(value = "/bie-packages/{id:\\d+}/uplifting", method = RequestMethod.POST)
//    public ResponseEntity upliftBiePackage(@AuthenticationPrincipal AuthenticatedPrincipal user,
//                                           @PathVariable("id") BiePackageId biePackageId,
//                                           @RequestBody UpliftBiePackageRequest request) throws ScoreDataAccessException {
//        request.setRequester(sessionService.asScoreUser(user));
//        request.setBiePackageId(biePackageId);
//
//        service.upliftBiePackage(request);
//        return ResponseEntity.noContent().build();
//    }
}
