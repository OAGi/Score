package org.oagi.score.gateway.http.api.passkey_management.service;

import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeySummary;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
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

    private static final long ADOPTION_CACHE_MILLIS = 60_000L;

    private record Adoption(double share, long at) {
    }

    private final byte[] decoyKey = randomKey();

    private volatile Adoption adoption;

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
     * For the login page, before anybody is signed in.
     * <p>
     * A login ID that does not exist gets a made-up answer instead of {@code false}: {@code true} for the same share
     * of names as the share of real accounts that have a passkey, chosen by a keyed hash so that a name always gets
     * the same answer. A {@code true} therefore does not prove the account exists, and neither does a {@code false}.
     * (The key is random per backend instance, so with several instances a name may be answered differently by each.)
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
                .orElseGet(() -> decoy(decoyKey, loginId, adoption(repository, currentRpId)));
    }

    private double adoption(PasskeyQueryRepository repository, String rpId) {
        Adoption snapshot = this.adoption;
        long now = System.currentTimeMillis();
        if (snapshot == null || now - snapshot.at() > ADOPTION_CACHE_MILLIS) {
            snapshot = new Adoption(repository.passkeyAdoption(rpId), now);
            this.adoption = snapshot;
        }
        return snapshot.share();
    }

    /**
     * @return a fixed pseudo-random answer for this name that is {@code true} for about {@code adoption} of all names
     */
    static boolean decoy(byte[] key, String loginId, double adoption) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] hash = mac.doFinal(loginId.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            int sample = ((hash[0] & 0xff) << 8) | (hash[1] & 0xff);
            return sample < adoption * 65536;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] randomKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return key;
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
