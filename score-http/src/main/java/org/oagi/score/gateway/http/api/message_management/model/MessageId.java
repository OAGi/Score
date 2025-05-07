package org.oagi.score.gateway.http.api.message_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a message.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record MessageId(BigInteger value) implements Id {

    @JsonCreator
    public static MessageId from(String value) {
        return new MessageId(new BigInteger(value));
    }

    @JsonCreator
    public static MessageId from(BigInteger value) {
        return new MessageId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
