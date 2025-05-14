package org.oagi.score.gateway.http.api.graph.controller;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.graph.model.FindUsagesRequest;
import org.oagi.score.gateway.http.api.graph.model.FindUsagesResponse;
import org.oagi.score.gateway.http.api.graph.model.Graph;
import org.oagi.score.gateway.http.api.graph.service.GraphService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class GraphController {

    @Autowired
    private GraphService graphService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/graphs/find_usages/{type}/{id:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FindUsagesResponse findUsages(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                         @PathVariable("type") String type,
                                         @PathVariable("id") BigInteger manifestId) {
        CcType ccType = CcType.valueOf(type.toUpperCase());
        switch (ccType) {
            case ACC:
                return graphService.findUsages(
                        sessionService.asScoreUser(user), new FindUsagesRequest(ccType, new AccManifestId(manifestId)));
            case ASCCP:
                return graphService.findUsages(
                        sessionService.asScoreUser(user), new FindUsagesRequest(ccType, new AsccpManifestId(manifestId)));
            case BCCP:
                return graphService.findUsages(
                        sessionService.asScoreUser(user), new FindUsagesRequest(ccType, new BccpManifestId(manifestId)));
            case DT:
                return graphService.findUsages(
                        sessionService.asScoreUser(user), new FindUsagesRequest(ccType, new DtManifestId(manifestId)));
        }

        throw new IllegalArgumentException("Unknown graph type " + type);
    }

    @RequestMapping(value = "/graphs/{type}/{id:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getGraph(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                        @PathVariable("type") String type,
                                        @PathVariable("id") BigInteger id,
                                        @RequestParam(value = "q", required = false) String query) {
        Graph graph;
        switch (type.toLowerCase()) {
            case "acc":
            case "extension":
                graph = graphService.getAccGraph(
                        sessionService.asScoreUser(user), new AccManifestId(id));
                break;

            case "asccp":
                graph = graphService.getAsccpGraph(
                        sessionService.asScoreUser(user), new AsccpManifestId(id), false);
                break;

            case "bccp":
                graph = graphService.getBccpGraph(
                        sessionService.asScoreUser(user), new BccpManifestId(id));
                break;

            case "dt":
                graph = graphService.getDtGraph(
                        sessionService.asScoreUser(user), new DtManifestId(id));
                break;

            case "top_level_asbiep":
                graph = graphService.getBieGraph(
                        sessionService.asScoreUser(user), new TopLevelAsbiepId(id));
                break;

            case "code_list":
                graph = graphService.getCodeListGraph(
                        sessionService.asScoreUser(user), new CodeListManifestId(id));
                break;

            default:
                throw new UnsupportedOperationException();
        }

        Map<String, Object> response = new HashMap();

        if (StringUtils.hasLength(query)) {
            Collection<List<String>> paths = graph.findPaths(type + id, query);
            response.put("query", query);
            response.put("paths", paths.stream()
                    .map(e -> e.stream()
                            .filter(item -> !item.matches("ascc\\d+|bcc\\d+|bdt\\d+"))
                            .collect(Collectors.joining(">"))
                    )
                    .collect(Collectors.toList()));
        } else {
            response.put("graph", graph);
        }

        return response;
    }

    @RequestMapping(value = "/graphs/uplift/{topLevelAsbiepId:[\\d]+}/{targetReleaseId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getUpliftGraph(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                              @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                                              @PathVariable("targetReleaseId") ReleaseId targetReleaseId) {

        AsccpSummaryRecord asccpSummary = graphService.getUpliftBie(
                sessionService.asScoreUser(user), topLevelAsbiepId, targetReleaseId);
        Graph graph;
        graph = graphService.getAsccpGraph(
                sessionService.asScoreUser(user), asccpSummary.asccpManifestId(), false);

        Map<String, Object> response = new HashMap();
        response.put("graph", graph);
        response.put("accManifestId", asccpSummary.roleOfAccManifestId());
        response.put("asccpManifestId", asccpSummary.asccpManifestId());
        return response;
    }
}
