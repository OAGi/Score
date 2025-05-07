package org.oagi.score.gateway.http.common.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.security.core.GrantedAuthority;

public enum ScoreRole implements GrantedAuthority {

    ANONYMOUS("anonymous"),
    ADMINISTRATOR("admin"),
    DEVELOPER("developer"),
    END_USER("end_user");

    private final String role;

    ScoreRole(String role) {
        this.role = role;
    }

    @JsonValue
    public String toString() {
        return this.role;
    }

    @Override
    public String getAuthority() {
        return this.role;
    }
}
