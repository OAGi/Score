package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.repo.api.bie.model.BieState;

public class InvalidBieStateException extends RuntimeException {

    private final BieState invalidState;

    public InvalidBieStateException(BieState invalidState) {
        this.invalidState = invalidState;
    }

    public InvalidBieStateException(String message, BieState invalidState) {
        super(message);
        this.invalidState = invalidState;
    }

    public InvalidBieStateException(Throwable cause, BieState invalidState) {
        super(cause);
        this.invalidState = invalidState;
    }

    public InvalidBieStateException(String message, Throwable cause, BieState invalidState) {
        super(message, cause);
        this.invalidState = invalidState;
    }

    public BieState getInvalidState() {
        return invalidState;
    }
}
