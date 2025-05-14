package org.oagi.score.gateway.http.api.cc_management.model.dt;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a DT_AWD_PRI.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record DtAwdPriId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static DtAwdPriId from(String value) {
        return new DtAwdPriId(new BigInteger(value));
    }

    @JsonCreator
    public static DtAwdPriId from(BigInteger value) {
        return new DtAwdPriId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
