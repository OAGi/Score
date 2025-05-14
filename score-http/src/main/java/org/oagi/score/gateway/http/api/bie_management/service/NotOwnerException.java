package org.oagi.score.gateway.http.api.bie_management.service;

public class NotOwnerException extends RuntimeException {

    public NotOwnerException() {
    }

    public NotOwnerException(String message) {
        super(message);
    }

    public NotOwnerException(Throwable cause) {
        super(cause);
    }

    public NotOwnerException(String message, Throwable cause) {
        super(message, cause);
    }

}
