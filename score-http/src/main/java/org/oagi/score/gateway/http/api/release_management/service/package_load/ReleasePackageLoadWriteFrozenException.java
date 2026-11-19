package org.oagi.score.gateway.http.api.release_management.service.package_load;

/** A write that was refused because writing is stopped while the schema of the application is swapped, and did not go on within the time it may wait. */
public class ReleasePackageLoadWriteFrozenException extends RuntimeException {

    public ReleasePackageLoadWriteFrozenException(String message, Throwable cause) {
        super(message, cause);
    }
}
