package org.oagi.score.e2e;

public class SignInException extends RuntimeException {

    public SignInException() {
    }

    public SignInException(String message) {
        super(message);
    }

    public SignInException(String message, Throwable cause) {
        super(message, cause);
    }

}
