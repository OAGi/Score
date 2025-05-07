package org.oagi.score.gateway.http.api.comment_management.controller.payload;

public record UpdateCommentRequest(
        String text, Boolean hide) {
}
