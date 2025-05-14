package org.oagi.score.gateway.http.api.code_list_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a code list value manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record CodeListValueManifestId(BigInteger value) implements ManifestId, Comparable<CodeListValueManifestId> {

    @JsonCreator
    public static CodeListValueManifestId from(String value) {
        return new CodeListValueManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static CodeListValueManifestId from(BigInteger value) {
        return new CodeListValueManifestId(value);
    }

    public int compareTo(CodeListValueManifestId id) {
        return value.compareTo(id.value());
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
