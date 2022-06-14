package org.oagi.score.repo.api.user.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScoreRole {

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
}
