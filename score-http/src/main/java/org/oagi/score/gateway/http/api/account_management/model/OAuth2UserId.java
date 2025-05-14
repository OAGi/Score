package org.oagi.score.gateway.http.api.account_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Represents a unique identifier for an app oauth2 user.ø
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record OAuth2UserId(BigInteger value) implements Id {

    @JsonCreator
    public static OAuth2UserId from(String value) {
        return new OAuth2UserId(new BigInteger(value));
    }

    @JsonCreator
    public static OAuth2UserId from(BigInteger value) {
        return new OAuth2UserId(value);
    }

    @JsonValue
    public BigInteger value() {
        return value;
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

    public boolean equals(OAuth2UserId other) {
        return Objects.equals(this, other);
    }

    public boolean equals(BigInteger other) {
        return Objects.equals(this.value, other);
    }

}
