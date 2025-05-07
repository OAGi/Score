package org.oagi.score.gateway.http.api.comment_management.repository;

import org.oagi.score.gateway.http.api.comment_management.model.CommentId;
import org.oagi.score.gateway.http.api.comment_management.model.CommentRecord;

import java.util.List;

public interface CommentQueryRepository {

    CommentRecord getCommentByCommentId(CommentId commentId);

    List<CommentRecord> getCommentsByReference(String reference);

    List<CommentRecord> getPrevComments(CommentId commentId);

}
