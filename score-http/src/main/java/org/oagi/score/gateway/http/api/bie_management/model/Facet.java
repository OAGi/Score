package org.oagi.score.gateway.http.api.bie_management.model;

import java.math.BigInteger;

public record Facet(BigInteger minLength,
                    BigInteger maxLength,
                    String pattern) {
}
