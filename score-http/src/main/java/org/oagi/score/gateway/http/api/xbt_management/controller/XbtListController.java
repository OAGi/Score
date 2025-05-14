package org.oagi.score.gateway.http.api.xbt_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.service.XbtQueryService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "XSD Built-in Types - Queries", description = "API for retrieving XSD built-in type-related data")
@RequestMapping("/xbts")
public class XbtListController {

    @Autowired
    private XbtQueryService xbtQueryService;

    @Autowired
    private SessionService sessionService;

    @GetMapping(value = "/summaries")
    public List<XbtSummaryRecord> getXbtSummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the release.")
            @RequestParam(name = "releaseId") ReleaseId releaseId) {

        return xbtQueryService.getXbtSummaryList(sessionService.asScoreUser(user), releaseId);
    }

}
