package org.oagi.score.gateway.http.api.passkey_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserId;

import java.time.LocalDateTime;

/**
 * A registered passkey (WebAuthn credential). {@code rpId} is the relying party ID it was registered for.
 * <p>
 * {@code attestationObject} is required for sign-in, not only kept for auditing: Spring Security reads the
 * credential's public key and previous counter from it when it verifies an assertion, and fails the sign-in of a
 * passkey without it. {@code signatureCount} is informational: Spring compares the authenticator's counter with the
 * one in the attestation object (0 at registration), not with this value, so cloned authenticators are not detected
 * through it (synced passkeys usually keep the counter at 0 anyway).
 */
public record PasskeyCredential(byte[] credentialId,
                                byte[] userHandle,
                                UserId userId,
                                String rpId,
                                byte[] publicKey,
                                long signatureCount,
                                boolean uvInitialized,
                                boolean backupEligible,
                                boolean backupState,
                                String transports,
                                String credentialType,
                                byte[] attestationObject,
                                byte[] attestationClientDataJson,
                                String label, // null when the passkey has no name
                                LocalDateTime creationTimestamp,
                                LocalDateTime lastUpdateTimestamp,
                                LocalDateTime lastUsedTimestamp,
                                String lastUsedUserAgent) {
}
