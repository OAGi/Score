package org.oagi.score.gateway.http.api.namespace_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a namespace.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record NamespaceId(BigInteger value) implements Id {

    @JsonCreator
    public static NamespaceId from(String value) {
        return new NamespaceId(new BigInteger(value));
    }

    @JsonCreator
    public static NamespaceId from(BigInteger value) {
        return new NamespaceId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
