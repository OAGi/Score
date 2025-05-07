package org.oagi.score.gateway.http.api.xbt_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a xbt manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record XbtManifestId(BigInteger value) implements ManifestId, Comparable<XbtManifestId> {

    @JsonCreator
    public static XbtManifestId from(String value) {
        return new XbtManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static XbtManifestId from(BigInteger value) {
        return new XbtManifestId(value);
    }

    public int compareTo(XbtManifestId id) {
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
