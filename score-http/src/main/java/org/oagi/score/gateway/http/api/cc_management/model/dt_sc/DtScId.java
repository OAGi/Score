package org.oagi.score.gateway.http.api.cc_management.model.dt_sc;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a DT SC.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record DtScId(BigInteger value) implements CoreComponentId, Comparable<DtScId> {

    @JsonCreator
    public static DtScId from(String value) {
        return new DtScId(new BigInteger(value));
    }

    @JsonCreator
    public static DtScId from(BigInteger value) {
        return new DtScId(value);
    }

    public int compareTo(DtScId id) {
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
