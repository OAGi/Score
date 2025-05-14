package org.oagi.score.gateway.http.api.cc_management.model.blob_content;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a blob content.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BlobContentId(BigInteger value) implements CoreComponentId, Comparable<BlobContentId> {

    @JsonCreator
    public static BlobContentId from(String value) {
        return new BlobContentId(new BigInteger(value));
    }

    @JsonCreator
    public static BlobContentId from(BigInteger value) {
        return new BlobContentId(value);
    }

    public int compareTo(BlobContentId id) {
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
