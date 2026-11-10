package org.oagi.score.gateway.http.api.exception_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.exception_management.model.ExceptionId;

/** Keeps what went wrong in the `exception` table, for an administrator to look into, with a number to refer to. */
public interface ExceptionCommandRepository {

    /**
     * Records an exception.
     *
     * @param tag        what to find it by (up to 50 characters), e.g. {@code release.drafting#1218}
     * @param message    what went wrong; the whole of it: it is cut where it is kept to be seen in a list
     * @param details    all there is to know of it as text (the message and the stack trace), kept compressed, or null when
     *                   there is none; an administrator reads it in SQL with {@code UNCOMPRESS(stacktrace)}
     * @param createdBy  the user who was working on when it happened, or null
     * @return the number of the record
     */
    ExceptionId create(String tag, String message, String details, UserId createdBy);
}
