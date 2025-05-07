package org.oagi.score.gateway.http.api.comment_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.comment_management.model.CommentRecord;
import org.oagi.score.gateway.http.api.comment_management.service.CommentQueryService;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Comment - Queries", description = "API for retrieving comment-related data")
@RequestMapping("/comments")
public class CommentQueryController {

    @Autowired
    private CommentQueryService commentQueryService;

    @Autowired
    private SessionService sessionService;

    @GetMapping(value = "/{reference}")
    public List<CommentRecord> getComments(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("reference") String reference) {

        if (!StringUtils.hasLength(reference)) {
            throw new IllegalArgumentException("'reference' parameter must not be empty.");
        }

        return commentQueryService.getComments(sessionService.asScoreUser(user), reference);
    }

}
