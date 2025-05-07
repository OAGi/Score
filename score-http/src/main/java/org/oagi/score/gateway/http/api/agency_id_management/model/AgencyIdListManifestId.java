package org.oagi.score.gateway.http.api.agency_id_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an agency ID list manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AgencyIdListManifestId(BigInteger value) implements ManifestId {

    @JsonCreator
    public static AgencyIdListManifestId from(String value) {
        return new AgencyIdListManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static AgencyIdListManifestId from(BigInteger value) {
        return new AgencyIdListManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
