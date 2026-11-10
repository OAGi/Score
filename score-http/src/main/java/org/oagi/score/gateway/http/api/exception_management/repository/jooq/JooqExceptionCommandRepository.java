package org.oagi.score.gateway.http.api.exception_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.exception_management.model.ExceptionId;
import org.oagi.score.gateway.http.api.exception_management.repository.ExceptionCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.time.LocalDateTime;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.EXCEPTION;

public class JooqExceptionCommandRepository extends JooqBaseRepository implements ExceptionCommandRepository {

    public JooqExceptionCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ExceptionId create(String tag, String message, String details, UserId createdBy) {
        return new ExceptionId(dslContext().insertInto(EXCEPTION)
                .set(EXCEPTION.TAG, tag.length() > 50 ? tag.substring(0, 50) : tag)
                .set(EXCEPTION.MESSAGE, ExceptionText.message(message))
                .set(EXCEPTION.STACKTRACE, ExceptionText.details(details))
                .set(EXCEPTION.CREATED_BY, createdBy != null ? valueOf(createdBy) : null)
                .set(EXCEPTION.CREATION_TIMESTAMP, LocalDateTime.now())
                .returning(EXCEPTION.EXCEPTION_ID)
                .fetchOne().getExceptionId().toBigInteger());
    }
}
