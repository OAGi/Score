package org.oagi.score.gateway.http.api.xbt_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a xbt.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record XbtId(BigInteger value) implements CoreComponentId, Comparable<XbtId> {

    @JsonCreator
    public static XbtId from(String value) {
        return new XbtId(new BigInteger(value));
    }

    @JsonCreator
    public static XbtId from(BigInteger value) {
        return new XbtId(value);
    }

    public int compareTo(XbtId id) {
        if (id == null || id.value() == null) {
            return (this.value == null) ? 0 : 1;
        }
        if (this.value == null) {
            return -1;
        }
        return value.compareTo(id.value());
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
