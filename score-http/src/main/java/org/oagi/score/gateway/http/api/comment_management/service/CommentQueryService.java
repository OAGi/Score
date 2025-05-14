package org.oagi.score.gateway.http.api.comment_management.service;

import org.oagi.score.gateway.http.api.comment_management.model.CommentRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    public List<CommentRecord> getComments(ScoreUser requester, String reference) {

        var query = repositoryFactory.commentQueryRepository(requester);
        List<CommentRecord> comments = query.getCommentsByReference(reference);
        return comments;
    }

}
