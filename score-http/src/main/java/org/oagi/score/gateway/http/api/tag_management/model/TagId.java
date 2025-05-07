package org.oagi.score.gateway.http.api.tag_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a tag.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record TagId(BigInteger value) implements Id {

    @JsonCreator
    public static TagId from(String value) {
        return new TagId(new BigInteger(value));
    }

    @JsonCreator
    public static TagId from(BigInteger value) {
        return new TagId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
