package org.oagi.score.gateway.http.api.tenant_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a tenant.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record TenantId(BigInteger value) implements Id {

    @JsonCreator
    public static TenantId from(String value) {
        return new TenantId(new BigInteger(value));
    }

    @JsonCreator
    public static TenantId from(BigInteger value) {
        return new TenantId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
