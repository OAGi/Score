package org.oagi.score.gateway.http.api.passkey_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserId;

/**
 * The WebAuthn identity of an application user: the opaque, random user handle stored in the authenticator.
 */
public record PasskeyUser(UserId userId, String loginId, String displayName, byte[] userHandle) {
}
