package org.oagi.score.gateway.http.api.passkey_management.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeySummary;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyProperties;
import org.oagi.score.gateway.http.security.secret.ApplicationSecretKeys;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Read-only access to registered passkeys.
 */
@Service
@Transactional(readOnly = true)
public class PasskeyQueryService implements InitializingBean {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private PasskeyProperties passkeyProperties;

    private static final long ADOPTION_CACHE_MILLIS = 60_000L;

    private record Adoption(double share, long at) {
    }

    private static final UserId NO_ACCOUNT = new UserId(BigInteger.ZERO);

    private static final String DECOY_KEY_PURPOSE = "passkey-lookup-decoy";

    /**
     * Where the key of the made-up answers comes from: the application secret key (SCORE_APP_SECRET_KEY). Optional
     * here: without it the key is random for each run.
     */
    @Autowired(required = false)
    private ApplicationSecretKeys applicationSecretKeys;

    private byte[] decoyKey = randomKey();

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
     * The key comes from SCORE_APP_SECRET_KEY, so every instance gives the same answer and a restart changes nothing.
     * Without it the key is random per run, and an attacker who probes before and after a restart can tell the names
     * whose answer changed (unknown) from the ones that did not.
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
                .orElseGet(() -> {
                    // the same kind of query a real account costs, so the two paths take about as long
                    repository.findCredentialsByUserId(NO_ACCOUNT);
                    return decoy(decoyKey, loginId, adoption(repository, currentRpId));
                });
    }

    @Override
    public void afterPropertiesSet() {
        Optional<byte[]> derived = (applicationSecretKeys == null)
                ? Optional.empty() : applicationSecretKeys.derive(DECOY_KEY_PURPOSE);
        if (derived.isPresent()) {
            this.decoyKey = derived.get();
        } else {
            logger.warn("SCORE_APP_SECRET_KEY is not set: the made-up answers of the passkey lookup change whenever the "
                    + "backend restarts, which lets someone who probes before and after tell unknown names from real ones.");
        }
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
