package org.oagi.score.gateway.http.api.release_management.service;

/** A component assigned to a release could not be moved to the state the release asks for. */
public class ComponentNotMovedException extends IllegalStateException {

    public ComponentNotMovedException(String message) {
        super(message);
    }
}
