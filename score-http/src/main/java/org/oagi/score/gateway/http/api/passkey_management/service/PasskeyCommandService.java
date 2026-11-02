package org.oagi.score.gateway.http.api.passkey_management.service;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyCommandRepository;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyQueryRepository;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

/**
 * Renames and removes passkeys.
 * <p>
 * Removing a passkey only deletes the server-side record. The private key stays on the user's device, which the
 * server cannot reach, but a deleted record can no longer be used to sign in.
 */
@Service
@Transactional
public class PasskeyCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private PasskeyProperties passkeyProperties;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PasskeyNotifier passkeyNotifier;

    /**
     * Renames one of the requester's own passkeys.
     */
    public void renamePasskey(ScoreUser requester, String id, String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Enter a name for the passkey.");
        }
        PasskeyCredential credential = ownedCredential(requester, id);
        repositoryFactory.passkeyCommandRepository(requester).updateLabel(credential.credentialId(), label.trim());
    }

    /**
     * Removes one of the requester's own passkeys. The last sign-in method cannot be removed: a user without a
     * password keeps at least one passkey.
     */
    public void deleteMyPasskey(ScoreUser requester, String id) {
        PasskeyCredential credential = ownedCredential(requester, id);
        PasskeyQueryRepository query = repositoryFactory.passkeyQueryRepository(requester);
        if (query.countCredentialsByUserId(requester.userId()) <= 1 && !query.hasPassword(requester.userId())) {
            throw new IllegalArgumentException(
                    "This is your only sign-in method. Set a password before removing your last passkey.");
        }
        repositoryFactory.passkeyCommandRepository(requester).deleteCredential(credential.credentialId());
        passkeyNotifier.passkeyRemoved(requester.userId());
    }

    /**
     * Administrator action: removes all passkeys of a user (for example a lost device), optionally ending the
     * user's current sessions as well.
     *
     * @return the number of passkeys removed
     */
    public int deletePasskeysOfUser(ScoreUser requester, UserId userId, boolean invalidateSessions) {
        requireAdministrator(requester);
        PasskeyQueryRepository query = repositoryFactory.passkeyQueryRepository(requester);
        String loginId = query.findLoginId(userId).orElseThrow(NotFoundException::new);
        int removed = repositoryFactory.passkeyCommandRepository(requester).deleteCredentialsByUserId(userId);
        if (invalidateSessions) {
            sessionService.invalidateByUsername(loginId);
        }
        passkeyNotifier.passkeysRemovedByAdministrator(userId, removed);
        return removed;
    }

    /**
     * Administrator action: removes every passkey registered for an RP ID other than the configured one (after
     * a domain change). Refused while passkey sign-in is not configured, so it cannot wipe everything.
     *
     * @return the number of passkeys removed
     */
    public int deleteStalePasskeys(ScoreUser requester) {
        requireAdministrator(requester);
        String rpId = passkeyProperties.current().getRpId();
        if (rpId == null || rpId.isBlank()) {
            throw new IllegalArgumentException("Passkey sign-in is not configured.");
        }
        PasskeyCommandRepository command = repositoryFactory.passkeyCommandRepository(requester);
        return command.deleteCredentialsOutsideRpId(rpId);
    }

    private PasskeyCredential ownedCredential(ScoreUser requester, String id) {
        byte[] credentialId = decode(id);
        PasskeyCredential credential = repositoryFactory.passkeyQueryRepository(requester)
                .findCredential(credentialId)
                .orElseThrow(NotFoundException::new);
        // A passkey that is not yours is reported as missing, so credential IDs cannot be probed.
        if (!credential.userId().equals(requester.userId())) {
            throw new NotFoundException();
        }
        return credential;
    }

    private static byte[] decode(String id) {
        try {
            return Base64.getUrlDecoder().decode(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid passkey ID.");
        }
    }

    private static void requireAdministrator(ScoreUser requester) {
        if (!requester.isAdministrator()) {
            throw new AccessControlException(requester);
        }
    }

}
