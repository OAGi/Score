package org.oagi.score.gateway.http.api.context_management.context_category.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a context category.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ContextCategoryId(BigInteger value) implements Id {

    @JsonCreator
    public static ContextCategoryId from(String value) {
        return new ContextCategoryId(new BigInteger(value));
    }

    @JsonCreator
    public static ContextCategoryId from(BigInteger value) {
        return new ContextCategoryId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
