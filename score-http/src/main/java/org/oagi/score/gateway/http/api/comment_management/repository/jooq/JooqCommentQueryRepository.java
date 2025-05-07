package org.oagi.score.gateway.http.api.comment_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.oagi.score.gateway.http.api.comment_management.model.CommentId;
import org.oagi.score.gateway.http.api.comment_management.model.CommentRecord;
import org.oagi.score.gateway.http.api.comment_management.repository.CommentQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collections;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.COMMENT;
import static org.springframework.util.StringUtils.hasLength;

public class JooqCommentQueryRepository extends JooqBaseRepository implements CommentQueryRepository {

    public JooqCommentQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public CommentRecord getCommentByCommentId(CommentId commentId) {
        if (commentId == null) {
            return null;
        }

        var queryBuilder = new GetCommentQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        COMMENT.COMMENT_ID.eq(valueOf(commentId)),
                        COMMENT.IS_DELETED.eq((byte) 0)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetCommentQueryBuilder {

        SelectOnConditionStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            COMMENT.COMMENT_ID, COMMENT.COMMENT_,
                            COMMENT.CREATION_TIMESTAMP,
                            COMMENT.LAST_UPDATE_TIMESTAMP,
                            COMMENT.IS_HIDDEN, COMMENT.PREV_COMMENT_ID
                    ), creatorFields()))
                    .from(COMMENT)
                    .join(creatorTable()).on(COMMENT.CREATED_BY.eq(creatorTablePk()));
        }

        RecordMapper<Record, CommentRecord> mapper() {
            return record -> new CommentRecord(
                    new CommentId(record.get(COMMENT.COMMENT_ID).toBigInteger()),
                    record.get(COMMENT.COMMENT_),
                    (byte) 1 == record.get(COMMENT.IS_HIDDEN),
                    (record.get(COMMENT.PREV_COMMENT_ID) != null) ? new CommentId(
                            record.get(COMMENT.PREV_COMMENT_ID).toBigInteger()) : null,
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.get(COMMENT.CREATION_TIMESTAMP))
                    ),
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.get(COMMENT.LAST_UPDATE_TIMESTAMP))
                    )
            );
        }

    }

    @Override
    public List<CommentRecord> getCommentsByReference(String reference) {
        if (!hasLength(reference)) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetCommentQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        COMMENT.REFERENCE.eq(reference),
                        COMMENT.IS_DELETED.eq((byte) 0)
                ))
                .orderBy(DSL.when(COMMENT.PREV_COMMENT_ID.isNotNull(), COMMENT.PREV_COMMENT_ID)
                        .else_(COMMENT.COMMENT_ID), COMMENT.CREATION_TIMESTAMP.asc())
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<CommentRecord> getPrevComments(CommentId commentId) {
        if (commentId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetCommentQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        COMMENT.PREV_COMMENT_ID.eq(valueOf(commentId)),
                        COMMENT.IS_DELETED.eq((byte) 0)
                ))
                .orderBy(COMMENT.LAST_UPDATE_TIMESTAMP.asc())
                .fetch(queryBuilder.mapper());
    }

}