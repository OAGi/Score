package org.oagi.score.repo.api.base;

public class ScoreDataAccessException extends RuntimeException {

    public ScoreDataAccessException() {
    }

    public ScoreDataAccessException(String message) {
        super(message);
    }

    public ScoreDataAccessException(Throwable cause) {
        super(cause);
    }

    public ScoreDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

}
