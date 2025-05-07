package org.oagi.score.gateway.http.api.agency_id_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an agency ID list value.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AgencyIdListValueId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static AgencyIdListValueId from(String value) {
        return new AgencyIdListValueId(new BigInteger(value));
    }

    @JsonCreator
    public static AgencyIdListValueId from(BigInteger value) {
        return new AgencyIdListValueId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
