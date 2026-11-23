package org.oagi.score.gateway.http.common.repository.jooq;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.ExecuteListenerProvider;
import org.jooq.impl.DefaultExecuteListener;
import org.oagi.score.gateway.http.common.model.base.DataValueTooLongException;
import org.oagi.score.gateway.http.common.model.base.DuplicateValueException;
import org.oagi.score.gateway.http.common.model.base.ReferencedRecordException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * The only place where a failed write is told apart: what the repositories throw for an integrity violation is one of the
 * database-independent exceptions of {@code common.model.base}. A duplicate is what Spring's translator for the database in
 * use reports as {@link DuplicateKeyException}, and a value longer than its column is the standard SQLSTATE 22001, so
 * a different database needs no change here.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataIntegrityExceptionTranslator implements ExecuteListenerProvider {

    static final String STRING_DATA_RIGHT_TRUNCATION = "22001";

    @Override
    public ExecuteListener provide() {
        return new DefaultExecuteListener() {
            @Override
            public void exception(ExecuteContext ctx) {
                RuntimeException translated = translate(ctx.exception());
                if (translated != null) {
                    ctx.exception(translated);
                }
            }
        };
    }

    /** @return the database-independent exception for an integrity violation, or null when it is something else. */
    static RuntimeException translate(Throwable exception) {
        if (!(exception instanceof DataIntegrityViolationException)) {
            return null;
        }
        if (exception instanceof DuplicateKeyException) {
            return new DuplicateValueException(exception);
        }
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && STRING_DATA_RIGHT_TRUNCATION.equals(sql.getSQLState())) {
                return new DataValueTooLongException(exception);
            }
        }
        return new ReferencedRecordException(exception);
    }
}
