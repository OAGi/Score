package org.oagi.score.gateway.http.api.bie_management.controller;

import org.oagi.score.gateway.http.api.bie_management.service.BieRepository;
import org.oagi.score.service.bie.BieReuseReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

@RestController
public class BieReportController {

    @Autowired
    private BieRepository bieRepository;

    @RequestMapping(value = "/profile_bie/reuse_report", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BieReuseReport> findTargetAsccpManifest(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return bieRepository.getBieReuseReport(null);
    }

    @RequestMapping(value = "/profile_bie/reuse_report/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BieReuseReport> findTargetAsccpManifest(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                        @PathVariable("id") BigInteger reusedTopLevelAsbiepId) {
        return bieRepository.getBieReuseReport(reusedTopLevelAsbiepId);
    }

}
