package org.oagi.score.gateway.http.api.cc_management.model.asccp;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an ASCCP.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AsccpId(BigInteger value) implements CoreComponentId, Comparable<AsccpId> {

    @JsonCreator
    public static AsccpId from(String value) {
        return new AsccpId(new BigInteger(value));
    }

    @JsonCreator
    public static AsccpId from(BigInteger value) {
        return new AsccpId(value);
    }

    public int compareTo(AsccpId id) {
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
