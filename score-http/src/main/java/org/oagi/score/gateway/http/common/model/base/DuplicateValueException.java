package org.oagi.score.gateway.http.common.model.base;

/**
 * Thrown by the repository layer, whatever database is behind it, so the layers above never read database error codes.
 */
public class DuplicateValueException extends ScoreDataAccessException {

    public DuplicateValueException(Throwable cause) {
        super("The operation could not be completed because the same value already exists.", cause);
    }

}
