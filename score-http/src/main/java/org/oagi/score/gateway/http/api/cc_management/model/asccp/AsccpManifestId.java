package org.oagi.score.gateway.http.api.cc_management.model.asccp;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an ASCCP manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AsccpManifestId(BigInteger value) implements ManifestId, Comparable<AsccpManifestId> {

    @JsonCreator
    public static AsccpManifestId from(String value) {
        return new AsccpManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static AsccpManifestId from(BigInteger value) {
        return new AsccpManifestId(value);
    }

    public int compareTo(AsccpManifestId id) {
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
