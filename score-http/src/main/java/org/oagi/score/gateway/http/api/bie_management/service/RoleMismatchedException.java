package org.oagi.score.gateway.http.api.bie_management.service;

public class RoleMismatchedException extends RuntimeException {

    public RoleMismatchedException() {
    }

    public RoleMismatchedException(String message) {
        super(message);
    }

    public RoleMismatchedException(Throwable cause) {
        super(cause);
    }

    public RoleMismatchedException(String message, Throwable cause) {
        super(message, cause);
    }

}
