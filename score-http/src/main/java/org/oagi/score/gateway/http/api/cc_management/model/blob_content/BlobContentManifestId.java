package org.oagi.score.gateway.http.api.cc_management.model.blob_content;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a blob content manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BlobContentManifestId(BigInteger value) implements ManifestId, Comparable<BlobContentManifestId> {

    @JsonCreator
    public static BlobContentManifestId from(String value) {
        return new BlobContentManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static BlobContentManifestId from(BigInteger value) {
        return new BlobContentManifestId(value);
    }

    public int compareTo(BlobContentManifestId id) {
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
