package org.oagi.score.gateway.http.api.release_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a release.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ReleaseId(BigInteger value) implements Id {

    @JsonCreator
    public static ReleaseId from(String value) {
        return new ReleaseId(new BigInteger(value));
    }

    @JsonCreator
    public static ReleaseId from(BigInteger value) {
        return new ReleaseId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
