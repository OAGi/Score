package org.oagi.score.gateway.http.api.comment.data;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {

    private long commentId;
    private String text;

    private String loginId;
    private LocalDateTime timestamp;

    private boolean hidden;
    private Long prevCommentId;

}
