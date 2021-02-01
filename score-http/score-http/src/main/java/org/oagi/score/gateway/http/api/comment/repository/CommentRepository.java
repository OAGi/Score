package org.oagi.score.gateway.http.api.comment.repository;

import lombok.Data;
import org.jooq.DSLContext;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.comment.data.Comment;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CommentRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_USER;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.COMMENT;

@Repository
public class CommentRepository {

    @Autowired
    private DSLContext dslContext;

    public List<Comment> getCommentsByReference(String reference) {
        return dslContext.select(
                COMMENT.COMMENT_ID, APP_USER.LOGIN_ID, COMMENT.COMMENT_,
                COMMENT.LAST_UPDATE_TIMESTAMP,
                COMMENT.IS_HIDDEN, COMMENT.PREV_COMMENT_ID)
                .from(COMMENT)
                .join(APP_USER).on(COMMENT.CREATED_BY.eq(APP_USER.APP_USER_ID))
                .where(COMMENT.REFERENCE.eq(reference), COMMENT.IS_DELETED.eq((byte) 0))
                .orderBy(DSL.when(COMMENT.PREV_COMMENT_ID.isNotNull(), COMMENT.PREV_COMMENT_ID)
                        .else_(COMMENT.COMMENT_ID), COMMENT.CREATION_TIMESTAMP.asc())
                .fetchStream()
                .map(e -> {
                    Comment comment = new Comment();
                    comment.setCommentId(e.get(COMMENT.COMMENT_ID).longValue());
                    comment.setLoginId(e.get(APP_USER.LOGIN_ID));
                    comment.setText(e.get(COMMENT.COMMENT_));
                    comment.setTimestamp(e.get(COMMENT.LAST_UPDATE_TIMESTAMP));
                    comment.setHidden(e.get(COMMENT.IS_HIDDEN) == (byte) 1);
                    ULong prevCommentId = e.get(COMMENT.PREV_COMMENT_ID);
                    if (prevCommentId != null) {
                        comment.setPrevCommentId(prevCommentId.longValue());
                    }
                    return comment;
                })
                .collect(Collectors.toList());
    }

    public List<Comment> getCommentsByPrevCommentId(long commentId) {
        return dslContext.select(
                COMMENT.COMMENT_ID, APP_USER.LOGIN_ID, COMMENT.COMMENT_,
                COMMENT.LAST_UPDATE_TIMESTAMP,
                COMMENT.IS_HIDDEN, COMMENT.PREV_COMMENT_ID)
                .from(COMMENT)
                .join(APP_USER).on(COMMENT.CREATED_BY.eq(APP_USER.APP_USER_ID))
                .where(COMMENT.PREV_COMMENT_ID.eq(ULong.valueOf(commentId)), COMMENT.IS_DELETED.eq((byte) 0))
                .orderBy(COMMENT.LAST_UPDATE_TIMESTAMP.asc())
                .fetchStream()
                .map(e -> {
                    Comment comment = new Comment();
                    comment.setCommentId(e.get(COMMENT.COMMENT_ID).longValue());
                    comment.setLoginId(e.get(APP_USER.LOGIN_ID));
                    comment.setText(e.get(COMMENT.COMMENT_));
                    comment.setTimestamp(e.get(COMMENT.LAST_UPDATE_TIMESTAMP));
                    comment.setHidden(e.get(COMMENT.IS_HIDDEN) == (byte) 1);
                    ULong prevCommentId = e.get(COMMENT.PREV_COMMENT_ID);
                    if (prevCommentId != null) {
                        comment.setPrevCommentId(prevCommentId.longValue());
                    }
                    return comment;
                })
                .collect(Collectors.toList());
    }

    public Comment getCommentByCommentId(long commentId) {
        return dslContext.select(
                COMMENT.COMMENT_ID, APP_USER.LOGIN_ID, COMMENT.COMMENT_.as("text"),
                COMMENT.LAST_UPDATE_TIMESTAMP.as("timestamp"),
                COMMENT.IS_HIDDEN, COMMENT.PREV_COMMENT_ID)
                .from(COMMENT)
                .join(APP_USER).on(COMMENT.CREATED_BY.eq(APP_USER.APP_USER_ID))
                .where(COMMENT.COMMENT_ID.eq(ULong.valueOf(commentId)), COMMENT.IS_DELETED.eq((byte) 0))
                .fetchOneInto(Comment.class);
    }

    @Data
    public class InsertCommentArguments {

        private String reference;
        private String text;
        private ULong prevCommentId;
        private ULong createdBy;

        public InsertCommentArguments setReference(String reference) {
            this.reference = reference;
            return this;
        }

        public InsertCommentArguments setText(String text) {
            this.text = text;
            return this;
        }

        public InsertCommentArguments setPrevCommentId(Long prevCommentId) {
            if (prevCommentId == null || prevCommentId <= 0L) {
                return this;
            }
            return setPrevCommentId(ULong.valueOf(prevCommentId));
        }

        public InsertCommentArguments setPrevCommentId(ULong prevCommentId) {
            this.prevCommentId = prevCommentId;
            return this;
        }

        public InsertCommentArguments setCreatedBy(BigInteger createdBy) {
            return setCreatedBy(ULong.valueOf(createdBy));
        }

        public InsertCommentArguments setCreatedBy(ULong createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public long execute() {
            return executeInsertComment(this);
        }
    }

    public InsertCommentArguments insertComment() {
        return new InsertCommentArguments();
    }

    private long executeInsertComment(InsertCommentArguments arguments) {
        CommentRecord record = new CommentRecord();
        LocalDateTime timestamp = LocalDateTime.now();

        record.setReference(arguments.getReference());
        record.setComment(arguments.getText());
        record.setIsHidden((byte) 0);
        if (arguments.getPrevCommentId() != null) {
            record.setPrevCommentId(arguments.getPrevCommentId());
        }
        record.setCreatedBy(arguments.getCreatedBy());
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        return dslContext.insertInto(COMMENT)
                .set(record)
                .returning().fetchOne().getCommentId().longValue();
    }

    public BigInteger getOwnerIdByCommentId(long commentId) {
        return dslContext.select(COMMENT.CREATED_BY)
                .from(COMMENT)
                .where(COMMENT.COMMENT_ID.eq(ULong.valueOf(commentId)))
                .fetchOptionalInto(BigInteger.class).orElse(BigInteger.ZERO);
    }

    @Data
    public class UpdateCommentArguments {

        private final ULong userId;
        private ULong commentId;

        private String text;
        private Boolean hide;
        private Boolean delete;

        public UpdateCommentArguments(BigInteger userId) {
            this(ULong.valueOf(userId));
        }

        public UpdateCommentArguments(ULong userId) {
            this.userId = userId;
        }

        public UpdateCommentArguments setCommentId(Long commentId) {
            if (commentId == null || commentId <= 0L) {
                return this;
            }
            return setCommentId(ULong.valueOf(commentId));
        }

        public UpdateCommentArguments setCommentId(ULong commentId) {
            this.commentId = commentId;
            return this;
        }

        public UpdateCommentArguments setText(String text) {
            this.text = text;
            return this;
        }

        public UpdateCommentArguments setHide(Boolean hide) {
            if (hide != null) {
                this.hide = hide;
            }
            return this;
        }

        public UpdateCommentArguments setDelete(Boolean delete) {
            if (delete != null) {
                this.delete = delete;
            }
            return this;
        }

        public void execute() {
            if (isDirty()) {
                executeUpdateComment(this);
            }
        }

        private boolean isDirty() {
            return (this.text != null || this.hide != null || this.delete != null);
        }
    }

    public UpdateCommentArguments updateComment(BigInteger userId) {
        return new UpdateCommentArguments(userId);
    }

    private void executeUpdateComment(UpdateCommentArguments arguments) {
        LocalDateTime timestamp = LocalDateTime.now();

        UpdateSetMoreStep<CommentRecord> step = dslContext.update(COMMENT)
                .set(COMMENT.LAST_UPDATE_TIMESTAMP, timestamp);

        String text = arguments.getText();
        if (text != null) {
            if (!StringUtils.hasLength(text)) {
                step = step.setNull(COMMENT.COMMENT_);
            } else {
                step = step.set(COMMENT.COMMENT_, text);
            }
        }

        if (arguments.getHide() != null) {
            step = step.set(COMMENT.IS_HIDDEN, (byte) (arguments.getHide() ? 1 : 0));
        }

        if (arguments.getDelete() != null) {
            step = step.set(COMMENT.IS_DELETED, (byte) (arguments.getDelete() ? 1 : 0));
        }

        step.where(COMMENT.COMMENT_ID.eq(arguments.getCommentId())).execute();
    }

}
