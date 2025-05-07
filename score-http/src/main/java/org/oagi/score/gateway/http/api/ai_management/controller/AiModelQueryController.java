package org.oagi.score.gateway.http.api.ai_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.ai_management.service.AiModelQueryService;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "AI - Queries", description = "API for retrieving AI-related data")
@RequestMapping("/ai")
public class AiModelQueryController {

    @Autowired
    private AiModelQueryService queryService;

    @Autowired
    private SessionService sessionService;

    @GetMapping(value = "/models")
    public List<String> getAvailableModels() {
        return queryService.getAvailableModels();
    }

    @GetMapping("/generate/asccp/{asccpManifestId:[\\d]+}/definition")
    public Map<String, String> generateDefinition(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId,
            @RequestParam("model") String model,
            @RequestParam(value = "originalText", required = false) String originalText) {

        if (!hasLength(model)) {
            throw new IllegalArgumentException("`model` parameter must not be empty.");
        }

        String content = queryService.generateDefinition(
                sessionService.asScoreUser(user), asccpManifestId, model, originalText);
        return Map.of("generation", content);

    }

    @GetMapping("/generate/acc/{accManifestId:[\\d]+}/definition")
    public Map<String, String> generateDefinition(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId,
            @RequestParam("model") String model,
            @RequestParam(value = "originalText", required = false) String originalText) {

        if (!hasLength(model)) {
            throw new IllegalArgumentException("`model` parameter must not be empty.");
        }

        String content = queryService.generateDefinition(
                sessionService.asScoreUser(user), accManifestId, model, originalText);
        return Map.of("generation", content);
    }

    @GetMapping("/generate/acc/{accManifestId:[\\d]+}/name")
    public Map<String, String> suggestName(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId,
            @RequestParam("model") String model,
            @RequestParam(value = "originalName", required = false) String originalName) {

        if (!hasLength(model)) {
            throw new IllegalArgumentException("`model` parameter must not be empty.");
        }

        String content = queryService.suggestName(
                sessionService.asScoreUser(user), accManifestId, model, originalName);
        return Map.of("generation", content);
    }

}
