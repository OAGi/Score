package org.oagi.score.gateway.http.api.release_management.model;

import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Identifier for a row in the {@code release_dep} table.
 *
 * @param value the underlying database identifier value.
 */
public record ReleaseDepId(BigInteger value) implements Id {
}
