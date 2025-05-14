package org.oagi.score.gateway.http.api.cc_management.model.dt;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a DT.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record DtId(BigInteger value) implements CoreComponentId, Comparable<DtId> {

    @JsonCreator
    public static DtId from(String value) {
        return new DtId(new BigInteger(value));
    }

    @JsonCreator
    public static DtId from(BigInteger value) {
        return new DtId(value);
    }

    public int compareTo(DtId id) {
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
