package org.oagi.score.gateway.http.api.cc_management.model.dt_sc;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a DT SC manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record DtScManifestId(BigInteger value) implements ManifestId, Comparable<DtScManifestId> {

    @JsonCreator
    public static DtScManifestId from(String value) {
        return new DtScManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static DtScManifestId from(BigInteger value) {
        return new DtScManifestId(value);
    }

    public int compareTo(DtScManifestId id) {
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
