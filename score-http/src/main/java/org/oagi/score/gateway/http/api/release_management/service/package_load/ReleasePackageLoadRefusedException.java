package org.oagi.score.gateway.http.api.release_management.service.package_load;

/** A load that is not started, or a step of it that is not taken, because something is not as it has to be; it says what. */
public class ReleasePackageLoadRefusedException extends RuntimeException {

    public ReleasePackageLoadRefusedException(String message) {
        super(message);
    }

    public ReleasePackageLoadRefusedException(String message, Throwable cause) {
        super(message, cause);
    }
}
