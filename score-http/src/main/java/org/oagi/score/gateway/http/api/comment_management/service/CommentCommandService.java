package org.oagi.score.gateway.http.api.comment_management.service;

import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.comment_management.model.CommentId;
import org.oagi.score.gateway.http.api.comment_management.model.CommentRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.event.CcEvent;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class CommentCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void postComments(ScoreUser requester, String reference, String text, CommentId prevCommentId) {

        var command = repositoryFactory.commentCommandRepository(requester);

        CommentId commentId = command.create(reference, text, prevCommentId);

        var query = repositoryFactory.commentQueryRepository(requester);

        CommentRecord comment = query.getCommentByCommentId(commentId);

        CcEvent event = new CcEvent();
        event.setAction("AddComment");
        event.addProperty("actor", requester.username());
        event.addProperty("text", comment.text());
        event.addProperty("prevCommentId", comment.prevCommentId());
        event.addProperty("commentId", commentId);
        event.addProperty("timestamp", comment.lastUpdated().when());
        List<String> parts = Arrays.asList(reference.split("(?<=\\D)-(?=\\d)"));
        if (parts.size() == 2) {
            simpMessagingTemplate.convertAndSend("/topic/" + parts.get(0).toLowerCase() + "/" + parts.get(1), event);
        }
    }

    public void updateComments(ScoreUser requester, CommentId commentId, String text) {

        var query = repositoryFactory.commentQueryRepository(requester);
        CommentRecord comment = query.getCommentByCommentId(commentId);
        if (comment == null) {
            throw new EmptyResultDataAccessException(1);
        }

        UserId userId = requester.userId();
        if (!comment.created().who().userId().equals(userId)) {
            throw new DataAccessForbiddenException("Only allowed to modify the comment by the owner.");
        }

        var command = repositoryFactory.commentCommandRepository(requester);
        command.update(commentId, text);
    }

    public void deleteComment(ScoreUser requester, CommentId commentId) {

        var query = repositoryFactory.commentQueryRepository(requester);
        CommentRecord comment = query.getCommentByCommentId(commentId);
        if (comment == null) {
            throw new EmptyResultDataAccessException(1);
        }

        UserId userId = requester.userId();
        if (!comment.created().who().userId().equals(userId)) {
            throw new DataAccessForbiddenException("Only allowed to modify the comment by the owner.");
        }

        List<CommentRecord> prevComments = query.getPrevComments(comment.commentId());

        var command = repositoryFactory.commentCommandRepository(requester);
        if (prevComments != null && !prevComments.isEmpty()) {
            command.markAsHidden(commentId);
        } else {
            command.markAsDeleted(commentId);
        }
    }
}
