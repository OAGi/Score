package org.oagi.score.gateway.http.common.model.base;

/**
 * Thrown by the repository layer, whatever database is behind it, so the layers above never read database error codes.
 */
public class DataValueTooLongException extends ScoreDataAccessException {

    public DataValueTooLongException(Throwable cause) {
        super("A value is too long.", cause);
    }

}
