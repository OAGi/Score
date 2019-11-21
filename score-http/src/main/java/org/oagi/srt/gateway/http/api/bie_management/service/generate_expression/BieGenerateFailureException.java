package org.oagi.srt.gateway.http.api.bie_management.service.generate_expression;

public class BieGenerateFailureException extends RuntimeException {

    public BieGenerateFailureException() {
    }

    public BieGenerateFailureException(String message) {
        super(message);
    }

    public BieGenerateFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
