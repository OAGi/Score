package org.oagi.score.gateway.http.api.comment_management.repository;

import org.oagi.score.gateway.http.api.comment_management.model.CommentId;

public interface CommentCommandRepository {

    CommentId create(String reference, String text, CommentId prevCommentId);

    boolean update(CommentId commentId, String text);

    boolean markAsHidden(CommentId commentId);

    boolean markAsDeleted(CommentId commentId);

}
