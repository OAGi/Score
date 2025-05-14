package org.oagi.score.gateway.http.api.comment_management.controller.payload;

import org.oagi.score.gateway.http.api.comment_management.model.CommentId;

public record PostCommentRequest(
        String text, CommentId prevCommentId) {
}
