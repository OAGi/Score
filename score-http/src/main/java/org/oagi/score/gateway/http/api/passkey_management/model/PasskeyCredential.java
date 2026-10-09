package org.oagi.score.gateway.http.api.passkey_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserId;

import java.time.LocalDateTime;

/**
 * A registered passkey (WebAuthn credential). {@code rpId} is the relying party ID it was registered for.
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
