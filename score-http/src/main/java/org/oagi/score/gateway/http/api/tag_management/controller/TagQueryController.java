package org.oagi.score.gateway.http.api.tag_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.tag_management.model.TagDetailsRecord;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.service.TagQueryService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for handling tag-related queries.
 */
@RestController
@Tag(name = "Tag - Queries", description = "API for retrieving tag-related data")
@RequestMapping("/tags")
public class TagQueryController {

    @Autowired
    private TagQueryService tagQueryService;

    @Autowired
    private SessionService sessionService;

    /**
     * Retrieves a list of tag summaries.
     *
     * @param user the authenticated user
     * @return a list of {@link TagSummaryRecord} objects
     */
    @Operation(summary = "Get Tag Summaries", description = "Retrieve a list of tag summaries accessible to the user.")
    @GetMapping(value = "/summaries")
    public List<TagSummaryRecord> getTagSummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user) {

        return tagQueryService.getTagSummaryList(sessionService.asScoreUser(user));
    }

    /**
     * Retrieves a list of tag details.
     *
     * @param user the authenticated user
     * @return a list of {@link TagDetailsRecord} objects
     */
    @Operation(summary = "Get Tag Details", description = "Retrieve a list of detailed tag records accessible to the user.")
    @GetMapping()
    public List<TagDetailsRecord> getTagDetailsList(
            @AuthenticationPrincipal AuthenticatedPrincipal user) {

        return tagQueryService.getTagDetailsList(sessionService.asScoreUser(user));
    }
}