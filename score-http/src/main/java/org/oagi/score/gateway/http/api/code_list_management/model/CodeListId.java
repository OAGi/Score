package org.oagi.score.gateway.http.api.code_list_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a code list.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record CodeListId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static CodeListId from(String value) {
        return new CodeListId(new BigInteger(value));
    }

    @JsonCreator
    public static CodeListId from(BigInteger value) {
        return new CodeListId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
