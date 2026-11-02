package org.oagi.score.gateway.http.api.passkey_management.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * What the Passkeys list shows. {@code id} is the base64url credential ID. {@code label} is null when the passkey was created without a name. {@code usable} is false when the
 * passkey was registered for a relying party ID other than the one configured now (for example after a domain
 * change), so the browser will not offer it. {@code userHandle} is the base64url WebAuthn user handle of the
 * owner, which the browser's Signal API needs to tell the credential manager which passkeys are still valid.
 * {@code lastUsedTimestamp} and {@code lastUsedUserAgent} are when the passkey last signed someone in and the
 * User-Agent header of that request (null until it has been used). {@code provider} names the passkey manager or authenticator holding
 * the passkey when it can be told from the registration data (for example "Bitwarden"), else "Unknown".
 * {@code providerIcon} is its light icon and {@code providerIconDark} its dark one, as base64 SVG data URIs, if it has them.
 */
public record PasskeySummary(String id,
                             String label,
                             LocalDateTime creationTimestamp,
                             LocalDateTime lastUpdateTimestamp,
                             boolean backupEligible,
                             boolean backedUp,
                             List<String> transports,
                             String rpId,
                             boolean usable,
                             String userHandle,
                             LocalDateTime lastUsedTimestamp,
                             String lastUsedUserAgent,
                             String provider,
                             String providerIcon,
                             String providerIconDark) {
}
