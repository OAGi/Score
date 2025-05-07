package org.oagi.score.gateway.http.api.comment_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.comment_management.controller.payload.PostCommentRequest;
import org.oagi.score.gateway.http.api.comment_management.controller.payload.UpdateCommentRequest;
import org.oagi.score.gateway.http.api.comment_management.model.CommentId;
import org.oagi.score.gateway.http.api.comment_management.service.CommentCommandService;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Comment - Commands", description = "API for creating, updating, and deleting comments")
@RequestMapping("/comments")
public class CommentCommandController {

    @Autowired
    private CommentCommandService commentCommandService;

    @Autowired
    private SessionService sessionService;

    @PostMapping(value = "/{reference}")
    public void postComment(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("reference") String reference,
            @RequestBody PostCommentRequest request) {

        if (!StringUtils.hasLength(reference)) {
            throw new IllegalArgumentException("'reference' parameter must not be empty.");
        }

        commentCommandService.postComments(
                sessionService.asScoreUser(user), reference, request.text(), request.prevCommentId());
    }

    @PutMapping(value = "/{commentId:[\\d]+}")
    public void updateComment(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("commentId") CommentId commentId,
            @RequestBody UpdateCommentRequest request) {

        commentCommandService.updateComments(sessionService.asScoreUser(user), commentId, request.text());
    }

    @DeleteMapping(value = "/{commentId:[\\d]+}")
    public void deleteComment(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("commentId") CommentId commentId) {

        commentCommandService.deleteComment(sessionService.asScoreUser(user), commentId);
    }

}
