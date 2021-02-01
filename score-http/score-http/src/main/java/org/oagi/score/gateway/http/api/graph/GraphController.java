package org.oagi.score.gateway.http.api.graph;

import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
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
                graph = graphService.getAccGraph(id);
                break;

            case "asccp":
                graph = graphService.getAsccpGraph(id, false);
                break;

            case "bccp":
                graph = graphService.getBccpGraph(id);
                break;

            case "top_level_asbiep":
                graph = graphService.getBieGraph(user, id);
                break;

            case "code_list":
                graph = graphService.getCodeListGraph(id);
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
                                        @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                        @PathVariable("targetReleaseId") BigInteger targetReleaseId) {

        AsccpManifestRecord asccpManifestRecord = graphService.getUpliftBie(user, topLevelAsbiepId, targetReleaseId);
        Graph graph;
        graph = graphService.getAsccpGraph(asccpManifestRecord.getAsccpManifestId().toBigInteger(), false);

        Map<String, Object> response = new HashMap();
        response.put("graph", graph);
        response.put("accManifestId", asccpManifestRecord.getRoleOfAccManifestId());
        response.put("asccpManifestId", asccpManifestRecord.getAsccpManifestId());
        return response;
    }
}
