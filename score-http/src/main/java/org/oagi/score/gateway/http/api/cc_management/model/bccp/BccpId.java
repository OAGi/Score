package org.oagi.score.gateway.http.api.cc_management.model.bccp;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a BCCP.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BccpId(BigInteger value) implements CoreComponentId, Comparable<BccpId> {

    @JsonCreator
    public static BccpId from(String value) {
        return new BccpId(new BigInteger(value));
    }

    @JsonCreator
    public static BccpId from(BigInteger value) {
        return new BccpId(value);
    }

    public int compareTo(BccpId id) {
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
