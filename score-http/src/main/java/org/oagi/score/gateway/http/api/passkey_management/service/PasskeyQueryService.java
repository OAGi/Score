package org.oagi.score.gateway.http.api.passkey_management.service;

import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeySummary;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Read-only access to registered passkeys.
 */
@Service
@Transactional(readOnly = true)
public class PasskeyQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private PasskeyProperties passkeyProperties;

    /**
     * Lists the requester's own passkeys.
     */
    public List<PasskeySummary> getMyPasskeys(ScoreUser requester) {
        String currentRpId = passkeyProperties.current().getRpId();
        return repositoryFactory.passkeyQueryRepository(requester)
                .findCredentialsByUserId(requester.userId()).stream()
                .map(e -> toSummary(e, currentRpId))
                .collect(Collectors.toList());
    }

    /**
     * Whether the account with this login ID has a passkey that can be used with the configured relying party ID.
     * For the login page, before anybody is signed in. Unknown accounts and accounts without a usable passkey are
     * indistinguishable (both false), so it does not say whether a login ID exists.
     */
    public boolean hasUsablePasskey(ScoreUser systemUser, String loginId) {
        String currentRpId = passkeyProperties.current().getRpId();
        if (!passkeyProperties.isEnabled() || loginId == null || loginId.isBlank()) {
            return false;
        }
        var repository = repositoryFactory.passkeyQueryRepository(systemUser);
        return repository.findUserIdByLoginId(loginId.trim())
                .map(userId -> repository.findCredentialsByUserId(userId).stream()
                        .anyMatch(e -> toSummary(e, currentRpId).usable()))
                .orElse(false);
    }

    static PasskeySummary toSummary(PasskeyCredential credential, String currentRpId) {
        List<String> transports = (credential.transports() == null || credential.transports().isBlank())
                ? Collections.emptyList()
                : Arrays.stream(credential.transports().split(",")).map(String::trim)
                .filter(e -> !e.isEmpty()).collect(Collectors.toList());
        PasskeyProviders.Provider provider = PasskeyProviders.find(credential.attestationObject());
        return new PasskeySummary(
                Base64.getUrlEncoder().withoutPadding().encodeToString(credential.credentialId()),
                credential.label(),
                credential.creationTimestamp(),
                credential.lastUpdateTimestamp(),
                credential.backupEligible(),
                credential.backupState(),
                transports,
                credential.rpId(),
                currentRpId != null && !currentRpId.isEmpty() && currentRpId.equalsIgnoreCase(credential.rpId()),
                Base64.getUrlEncoder().withoutPadding().encodeToString(credential.userHandle()),
                credential.lastUsedTimestamp(),
                credential.lastUsedUserAgent(),
                provider.name(),
                provider.iconLight(),
                provider.iconDark());
    }

}
