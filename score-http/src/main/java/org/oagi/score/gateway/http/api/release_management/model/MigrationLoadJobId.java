package org.oagi.score.gateway.http.api.release_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/** The identifier of a load of a release package. */
public record MigrationLoadJobId(BigInteger value) implements Id {

    @JsonCreator
    public static MigrationLoadJobId from(String value) {
        return new MigrationLoadJobId(new BigInteger(value));
    }

    @JsonCreator
    public static MigrationLoadJobId from(BigInteger value) {
        return new MigrationLoadJobId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
