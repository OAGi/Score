package org.oagi.score.gateway.http.api.release_management.service.migrationload;

/** A load that is not started, or a step of it that is not taken, because something is not as it has to be; it says what. */
public class MigrationLoadRefusedException extends RuntimeException {

    public MigrationLoadRefusedException(String message) {
        super(message);
    }

    public MigrationLoadRefusedException(String message, Throwable cause) {
        super(message, cause);
    }
}
