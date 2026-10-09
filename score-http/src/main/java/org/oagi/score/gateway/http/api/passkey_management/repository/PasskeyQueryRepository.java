package org.oagi.score.gateway.http.api.passkey_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyUser;

import java.util.List;
import java.util.Optional;

public interface PasskeyQueryRepository {

    Optional<UserId> findUserIdByLoginId(String loginId);

    Optional<String> findLoginId(UserId userId);

    Optional<PasskeyUser> findPasskeyUserByHandle(byte[] userHandle);

    Optional<PasskeyUser> findPasskeyUserByLoginId(String loginId);

    Optional<PasskeyCredential> findCredential(byte[] credentialId);

    List<PasskeyCredential> findCredentialsByUserHandle(byte[] userHandle);

    List<PasskeyCredential> findCredentialsByUserId(UserId userId);

    int countCredentialsByUserId(UserId userId);

    /**
     * Whether the user can still sign in with a password (a password is stored for the account).
     */
    boolean hasPassword(UserId userId);

}
