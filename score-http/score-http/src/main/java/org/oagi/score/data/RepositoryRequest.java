package org.oagi.score.data;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.time.LocalDateTime;

public class RepositoryRequest {

    private final AuthenticatedPrincipal user;
    private final LocalDateTime localDateTime;
    private boolean propagation;

    public RepositoryRequest(AuthenticatedPrincipal user) {
        this(user, LocalDateTime.now());
    }

    public RepositoryRequest(AuthenticatedPrincipal user,
                             LocalDateTime localDateTime) {
        this.user = user;
        this.localDateTime = localDateTime;
    }

    public AuthenticatedPrincipal getUser() {
        return user;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public boolean isPropagation() {
        return propagation;
    }

    public void setPropagation(boolean propagation) {
        this.propagation = propagation;
    }
}
