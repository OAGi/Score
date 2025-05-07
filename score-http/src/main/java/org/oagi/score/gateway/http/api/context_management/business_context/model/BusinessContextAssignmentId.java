package org.oagi.score.gateway.http.api.context_management.business_context.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a business context assignment.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BusinessContextAssignmentId(BigInteger value) implements Id {

    @JsonCreator
    public static BusinessContextAssignmentId from(String value) {
        return new BusinessContextAssignmentId(new BigInteger(value));
    }

    @JsonCreator
    public static BusinessContextAssignmentId from(BigInteger value) {
        return new BusinessContextAssignmentId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
