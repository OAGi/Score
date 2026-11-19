package org.oagi.score.gateway.http.api.release_management.service.package_load.format;

import java.math.BigInteger;

/**
 * What is done with a table, and by which column the records of the users are told, when it is {@link CopyKind#FROM_DELTA}.
 *
 * @param ownBelow when given, the records numbered below it are the instance's own too (the package does not carry them, and leaves what
 *                 the instance has), as the first account of an instance is
 */
public record CopyStep(String table, CopyKind kind, String idColumn, BigInteger ownBelow) {
}
