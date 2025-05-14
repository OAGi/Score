package org.oagi.score.gateway.http.api.code_list_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a code list value.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record CodeListValueId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static CodeListValueId from(String value) {
        return new CodeListValueId(new BigInteger(value));
    }

    @JsonCreator
    public static CodeListValueId from(BigInteger value) {
        return new CodeListValueId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
