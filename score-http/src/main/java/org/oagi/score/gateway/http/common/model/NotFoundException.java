package org.oagi.score.gateway.http.common.model;

import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

/**
 * Signals that a requested HTTP resource does not exist or is no longer
 * available.
 *
 * <p>Use this at the controller boundary when an API should return
 * {@code 404 Not Found}. It is intended for user-facing request handling
 * when a requested resource cannot be found.</p>
 */
public class NotFoundException extends ScoreDataAccessException {

    /**
     * Creates a not-found exception without a detail message.
     */
    public NotFoundException() {
    }

    /**
     * Creates a not-found exception with a detail message.
     *
     * @param message description of the missing resource
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a not-found exception with an underlying cause.
     *
     * @param cause underlying cause
     */
    public NotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a not-found exception with both a detail message and cause.
     *
     * @param message description of the missing resource
     * @param cause underlying cause
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
