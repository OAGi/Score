package org.oagi.score.gateway.http.api.release_management.repository;

import java.math.BigInteger;

/** A user account, as far as a load tells one from another: its number, the name it logs in with, and whether it is a developer's. */
public record Account(BigInteger id, String loginId, boolean developer) {
}
