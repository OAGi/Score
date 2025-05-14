package org.oagi.score.gateway.http.api.code_list_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a code list manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record CodeListManifestId(BigInteger value) implements ManifestId {

    @JsonCreator
    public static CodeListManifestId from(String value) {
        return new CodeListManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static CodeListManifestId from(BigInteger value) {
        return new CodeListManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
