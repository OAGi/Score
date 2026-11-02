package org.oagi.score.gateway.http.security.secret;

/** Signals that an encrypted-secret operation cannot run until a key is configured. */
public class ApplicationSecretUnavailableException extends IllegalStateException {

    public ApplicationSecretUnavailableException(String message) {
        super(message);
    }
}
