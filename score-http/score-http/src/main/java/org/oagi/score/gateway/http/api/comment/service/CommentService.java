package org.oagi.score.gateway.http.api.comment.service;

import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.cc_management.data.CcEvent;
import org.oagi.score.gateway.http.api.comment.data.Comment;
import org.oagi.score.gateway.http.api.comment.data.GetCommentRequest;
import org.oagi.score.gateway.http.api.comment.data.PostCommentRequest;
import org.oagi.score.gateway.http.api.comment.data.UpdateCommentRequest;
import org.oagi.score.gateway.http.api.comment.repository.CommentRepository;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    @Autowired
    private CommentRepository repository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public List<Comment> getComments(AuthenticatedPrincipal user, GetCommentRequest request) {
        List<Comment> comments = repository.getCommentsByReference(request.getReference());
        return comments;
    }

    @Transactional
    public void postComments(AuthenticatedPrincipal user, PostCommentRequest request) {
        BigInteger userId = sessionService.userId(user);

        long commentId = repository.insertComment()
                .setReference(request.getReference())
                .setText(request.getText())
                .setPrevCommentId(request.getPrevCommentId())
                .setCreatedBy(userId)
                .execute();

        Comment comment = repository.getCommentByCommentId(commentId);

        CcEvent event = new CcEvent();
        event.setAction("AddComment");
        event.addProperty("actor", user.getName());
        event.addProperty("text", comment.getText());
        event.addProperty("prevCommentId", comment.getPrevCommentId());
        event.addProperty("commentId", commentId);
        event.addProperty("timestamp", comment.getTimestamp());
        List<String> parts = Arrays.asList(request.getReference().split("(?<=\\D)-(?=\\d)"));
        if (parts.size() == 2) {
            simpMessagingTemplate.convertAndSend("/topic/" + parts.get(0).toLowerCase() + "/" + parts.get(1), event);
        }
    }

    @Transactional
    public void updateComments(AuthenticatedPrincipal user, UpdateCommentRequest request) {
        BigInteger userId = sessionService.userId(user);
        BigInteger ownerId = repository.getOwnerIdByCommentId(request.getCommentId());
        if (ownerId.equals(BigInteger.ZERO)) {
            throw new EmptyResultDataAccessException(1);
        }

        if (!ownerId.equals(userId)) {
            throw new DataAccessForbiddenException("Only allowed to modify the comment by the owner.");
        }

        CommentRepository.UpdateCommentArguments updateCommentArguments = repository.updateComment(userId);
        updateCommentArguments.setCommentId(request.getCommentId());
        if (request.getText() != null) {
            updateCommentArguments.setText(request.getText());
        }

        if (request.getDelete() != null) {
            if (repository.getCommentsByPrevCommentId(request.getCommentId()).size() > 0) {
                updateCommentArguments.setHide(request.getDelete());
            } else {
                updateCommentArguments.setDelete(request.getDelete());
            }
        }
        updateCommentArguments.execute();
    }
}
