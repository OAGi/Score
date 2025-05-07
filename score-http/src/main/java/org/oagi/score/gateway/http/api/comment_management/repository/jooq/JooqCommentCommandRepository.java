package org.oagi.score.gateway.http.api.comment_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.UpdateSetMoreStep;
import org.oagi.score.gateway.http.api.comment_management.model.CommentId;
import org.oagi.score.gateway.http.api.comment_management.repository.CommentCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.time.LocalDateTime;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.COMMENT;

public class JooqCommentCommandRepository extends JooqBaseRepository implements CommentCommandRepository {

    public JooqCommentCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public CommentId create(String reference, String text, CommentId prevCommentId) {
        org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.CommentRecord record = new org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.CommentRecord();

        record.setReference(reference);
        record.setComment(text);
        record.setIsHidden((byte) 0);
        if (prevCommentId != null) {
            record.setPrevCommentId(valueOf(prevCommentId));
        }
        record.setCreatedBy(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        return new CommentId(
                dslContext().insertInto(COMMENT)
                        .set(record)
                        .returning()
                        .fetchOne().getCommentId().toBigInteger());
    }

    @Override
    public boolean update(CommentId commentId, String text) {
        LocalDateTime timestamp = LocalDateTime.now();

        UpdateSetMoreStep step = dslContext().update(COMMENT)
                .set(COMMENT.LAST_UPDATE_TIMESTAMP, timestamp);

        if (!StringUtils.hasLength(text)) {
            step = step.setNull(COMMENT.COMMENT_);
        } else {
            step = step.set(COMMENT.COMMENT_, text);
        }

        int numOfUpdatedRecords = step.where(COMMENT.COMMENT_ID.eq(valueOf(commentId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean markAsHidden(CommentId commentId) {
        LocalDateTime timestamp = LocalDateTime.now();

        UpdateSetMoreStep step = dslContext().update(COMMENT)
                .set(COMMENT.LAST_UPDATE_TIMESTAMP, timestamp);

        step = step.set(COMMENT.IS_HIDDEN, (byte) 1);

        int numOfUpdatedRecords = step.where(COMMENT.COMMENT_ID.eq(valueOf(commentId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean markAsDeleted(CommentId commentId) {
        LocalDateTime timestamp = LocalDateTime.now();

        UpdateSetMoreStep step = dslContext().update(COMMENT)
                .set(COMMENT.LAST_UPDATE_TIMESTAMP, timestamp);

        step = step.set(COMMENT.IS_DELETED, (byte) 1);

        int numOfUpdatedRecords = step.where(COMMENT.COMMENT_ID.eq(valueOf(commentId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

}
