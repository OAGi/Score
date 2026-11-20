package org.oagi.score.gateway.http.api.release_management.repository;

import java.math.BigInteger;

/** A namespace, as far as a load tells one from another: its number, the library it is in, and its URI. */
public record Namespace(BigInteger id, BigInteger libraryId, String uri) {
}
