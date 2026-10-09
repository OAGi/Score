package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyCommandRepository;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores passkeys in {@code webauthn_credential}. A new credential is stamped with the RP ID configured at
 * registration time ({@code rp_id}); Spring's credential record has no such field, so it is added here.
 */
@Component
@Transactional
public class ScoreUserCredentialRepository implements UserCredentialRepository {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PasskeyProperties passkeyProperties;

    private ScoreUser system() {
        return sessionService.getScoreSystemUser();
    }

    @Override
    @Transactional(readOnly = true)
    public CredentialRecord findByCredentialId(Bytes credentialId) {
        return query().findCredential(credentialId.getBytes())
                .map(ScoreUserCredentialRepository::toRecord)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CredentialRecord> findByUserId(Bytes userId) {
        return query().findCredentialsByUserHandle(userId.getBytes()).stream()
                .map(ScoreUserCredentialRepository::toRecord)
                .collect(Collectors.toList());
    }

    @Override
    public void save(CredentialRecord record) {
        byte[] credentialId = record.getCredentialId().getBytes();
        PasskeyCommandRepository command = repositoryFactory.passkeyCommandRepository(system());
        if (query().findCredential(credentialId).isPresent()) {
            command.updateAuthenticatorData(credentialId, record.getSignatureCount(),
                    record.isUvInitialized(), record.isBackupEligible(), record.isBackupState(),
                    currentUserAgent());
            return;
        }

        String rpId = passkeyProperties.current().getRpId();
        command.insertCredential(new PasskeyCredential(
                credentialId,
                record.getUserEntityUserId().getBytes(),
                null,
                rpId,
                record.getPublicKey().getBytes(),
                record.getSignatureCount(),
                record.isUvInitialized(),
                record.isBackupEligible(),
                record.isBackupState(),
                joinTransports(record.getTransports()),
                record.getCredentialType().getValue(),
                (record.getAttestationObject() != null) ? record.getAttestationObject().getBytes() : null,
                (record.getAttestationClientDataJSON() != null) ? record.getAttestationClientDataJSON().getBytes() : null,
                labelOrNull(record.getLabel()),
                null,
                null,
                null,
                null));
    }

    @Override
    public void delete(Bytes credentialId) {
        repositoryFactory.passkeyCommandRepository(system()).deleteCredential(credentialId.getBytes());
    }

    /**
     * The User-Agent of the request being served, which is the sign-in that refreshes a passkey. Trimmed to what
     * the column holds; null outside a request.
     */
    static String currentUserAgent() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        String userAgent = attributes.getRequest().getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        return userAgent.length() > 512 ? userAgent.substring(0, 512) : userAgent;
    }

    private PasskeyQueryRepository query() {
        return repositoryFactory.passkeyQueryRepository(system());
    }

    /** A passkey is created without a name: the browser sends an empty one, which is stored as NULL. */
    static String labelOrNull(String label) {
        return (label == null || label.isBlank()) ? null : label.trim();
    }

    static String joinTransports(Set<AuthenticatorTransport> transports) {
        if (transports == null || transports.isEmpty()) {
            return null;
        }
        return transports.stream().map(AuthenticatorTransport::getValue).collect(Collectors.joining(","));
    }

    static Set<AuthenticatorTransport> splitTransports(String transports) {
        Set<AuthenticatorTransport> result = new LinkedHashSet<>();
        if (transports != null && !transports.isBlank()) {
            Arrays.stream(transports.split(","))
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .map(AuthenticatorTransport::valueOf)
                    .forEach(result::add);
        }
        return result;
    }

    private static CredentialRecord toRecord(PasskeyCredential credential) {
        ZoneId zone = ZoneId.systemDefault();
        return ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.valueOf(credential.credentialType()))
                .credentialId(new Bytes(credential.credentialId()))
                .userEntityUserId(new Bytes(credential.userHandle()))
                .publicKey(new ImmutablePublicKeyCose(credential.publicKey()))
                .signatureCount(credential.signatureCount())
                .uvInitialized(credential.uvInitialized())
                .transports(splitTransports(credential.transports()))
                .backupEligible(credential.backupEligible())
                .backupState(credential.backupState())
                .attestationObject((credential.attestationObject() != null) ? new Bytes(credential.attestationObject()) : null)
                .attestationClientDataJSON((credential.attestationClientDataJson() != null)
                        ? new Bytes(credential.attestationClientDataJson()) : null)
                .created(credential.creationTimestamp().atZone(zone).toInstant())
                .lastUsed(credential.lastUpdateTimestamp().atZone(zone).toInstant())
                .label(credential.label() != null ? credential.label() : "")
                .build();
    }

}
