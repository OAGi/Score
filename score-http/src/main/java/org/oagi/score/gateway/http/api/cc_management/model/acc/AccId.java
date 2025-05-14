package org.oagi.score.gateway.http.api.cc_management.model.acc;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an ACC.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AccId(BigInteger value) implements CoreComponentId, Comparable<AccId> {

    @JsonCreator
    public static AccId from(String value) {
        return new AccId(new BigInteger(value));
    }

    @JsonCreator
    public static AccId from(BigInteger value) {
        return new AccId(value);
    }

    public int compareTo(AccId id) {
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
