package org.oagi.score.gateway.http.api.release_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/** The identifier of a load of a release package. */
public record ReleasePackageLoadJobId(BigInteger value) implements Id {

    @JsonCreator
    public static ReleasePackageLoadJobId from(String value) {
        return new ReleasePackageLoadJobId(new BigInteger(value));
    }

    @JsonCreator
    public static ReleasePackageLoadJobId from(BigInteger value) {
        return new ReleasePackageLoadJobId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
