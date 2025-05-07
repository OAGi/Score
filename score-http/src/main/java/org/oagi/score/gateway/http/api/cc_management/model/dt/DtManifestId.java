package org.oagi.score.gateway.http.api.cc_management.model.dt;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a DT manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record DtManifestId(BigInteger value) implements ManifestId, Comparable<DtManifestId> {

    @JsonCreator
    public static DtManifestId from(String value) {
        return new DtManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static DtManifestId from(BigInteger value) {
        return new DtManifestId(value);
    }

    public int compareTo(DtManifestId id) {
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
