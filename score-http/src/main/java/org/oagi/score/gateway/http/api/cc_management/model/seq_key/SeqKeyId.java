package org.oagi.score.gateway.http.api.cc_management.model.seq_key;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a sequence key.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record SeqKeyId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static SeqKeyId from(String value) {
        return new SeqKeyId(new BigInteger(value));
    }

    @JsonCreator
    public static SeqKeyId from(BigInteger value) {
        return new SeqKeyId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
