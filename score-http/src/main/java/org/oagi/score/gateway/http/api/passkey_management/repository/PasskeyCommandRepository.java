package org.oagi.score.gateway.http.api.passkey_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;

public interface PasskeyCommandRepository {

    void insertPasskeyUser(UserId userId, byte[] userHandle);

    /**
     * Inserts a credential for the passkey user identified by {@code credential.userHandle()}.
     */
    void insertCredential(PasskeyCredential credential);

    /**
     * Stores the authenticator data refreshed by a sign-in and marks the passkey as used now, from the client
     * identified by {@code userAgent} (may be null).
     */
    void updateAuthenticatorData(byte[] credentialId, long signatureCount,
                                 boolean uvInitialized, boolean backupEligible, boolean backupState,
                                 String userAgent);

    int updateLabel(byte[] credentialId, String label);

    int deleteCredential(byte[] credentialId);

    int deleteCredentialsByUserId(UserId userId);

    /**
     * Deletes passkeys registered for a relying party ID other than {@code rpId}.
     */
    int deleteCredentialsOutsideRpId(String rpId);

    /**
     * Deletes the passkey user with the given handle and all of its credentials.
     */
    void deletePasskeyUserByHandle(byte[] userHandle);

}
