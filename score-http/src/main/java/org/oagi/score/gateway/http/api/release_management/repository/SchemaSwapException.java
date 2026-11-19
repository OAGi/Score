package org.oagi.score.gateway.http.api.release_management.repository;

/** A swap of schemas that did not go through, with what state the schemas are left in. */
public class SchemaSwapException extends RuntimeException {

    public SchemaSwapException(String message) {
        super(message);
    }

    public SchemaSwapException(String message, Throwable cause) {
        super(message, cause);
    }
}
