package org.oagi.score.gateway.http.api.agency_id_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an agency ID list.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AgencyIdListId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static AgencyIdListId from(String value) {
        return new AgencyIdListId(new BigInteger(value));
    }

    @JsonCreator
    public static AgencyIdListId from(BigInteger value) {
        return new AgencyIdListId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
