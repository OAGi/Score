package org.oagi.score.gateway.http.api.comment_management.model;

import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record CommentRecord(
        CommentId commentId,
        String text,

        boolean hidden,
        CommentId prevCommentId,

        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
