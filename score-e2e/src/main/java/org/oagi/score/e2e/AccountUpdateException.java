package org.oagi.score.e2e;

public class AccountUpdateException extends RuntimeException {

    public AccountUpdateException() {
    }

    public AccountUpdateException(String message) {
        super(message);
    }

    public AccountUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
