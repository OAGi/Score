package org.oagi.score.gateway.http.api.cc_management.model.bccp;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a BCCP manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BccpManifestId(BigInteger value) implements ManifestId, Comparable<BccpManifestId> {

    @JsonCreator
    public static BccpManifestId from(String value) {
        return new BccpManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static BccpManifestId from(BigInteger value) {
        return new BccpManifestId(value);
    }

    public int compareTo(BccpManifestId id) {
        if (id == null || id.value() == null) {
            return (this.value == null) ? 0 : 1;
        }
        if (this.value == null) {
            return -1;
        }
        return value.compareTo(id.value());
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
