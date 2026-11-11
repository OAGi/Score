package org.oagi.score.gateway.http.api.log_management.model;

/**
 * The history (log) a revision, an amendment or a cancel works from cannot be found, which only happens when it was
 * removed from the database by hand. It is reported to the user, who may let a new history be written and go on.
 */
public class LogNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "LOG_NOT_FOUND";

    public LogNotFoundException(String message) {
        super(message);
    }
}
