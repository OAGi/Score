package org.oagi.score.gateway.http.api.cc_management.model.dt_sc;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a DT_SC_AWD_PRI.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record DtScAwdPriId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static DtScAwdPriId from(String value) {
        return new DtScAwdPriId(new BigInteger(value));
    }

    @JsonCreator
    public static DtScAwdPriId from(BigInteger value) {
        return new DtScAwdPriId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
