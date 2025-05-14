package org.oagi.score.gateway.http.api.library_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a library.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record LibraryId(BigInteger value) implements Id {

    @JsonCreator
    public static LibraryId from(String value) {
        return new LibraryId(new BigInteger(value));
    }

    @JsonCreator
    public static LibraryId from(BigInteger value) {
        return new LibraryId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

}
