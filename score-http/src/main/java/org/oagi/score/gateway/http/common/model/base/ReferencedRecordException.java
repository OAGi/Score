package org.oagi.score.gateway.http.common.model.base;

/**
 * Thrown by the repository layer, whatever database is behind it, so the layers above never read database error codes.
 */
public class ReferencedRecordException extends ScoreDataAccessException {

    public ReferencedRecordException(Throwable cause) {
        super("The operation could not be completed because the record is referenced by other data.", cause);
    }

}
