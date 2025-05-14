package org.oagi.score.gateway.http.common.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record Guid(String value) {

    public Guid {
        requireNonNull(value, "Guid value cannot be null");
        if (!value.matches("[0-9a-f]{32}")) {
            throw new IllegalArgumentException("Invalid Guid: " + value + ". Guid must be a 32-character hexadecimal string (lowercase).");
        }
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    public static Guid create() {
        return new Guid(UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Guid guid = (Guid) o;
        return Objects.equals(value, guid.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
