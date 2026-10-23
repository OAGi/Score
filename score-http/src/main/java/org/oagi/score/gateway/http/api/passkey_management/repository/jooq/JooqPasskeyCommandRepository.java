package org.oagi.score.gateway.http.api.passkey_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.time.LocalDateTime;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.APP_WEBAUTHN_USER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.WEBAUTHN_CREDENTIAL;

public class JooqPasskeyCommandRepository extends JooqBaseRepository implements PasskeyCommandRepository {

    public JooqPasskeyCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public void insertPasskeyUser(UserId userId, byte[] userHandle) {
        LocalDateTime now = LocalDateTime.now();
        dslContext().insertInto(APP_WEBAUTHN_USER)
                .set(APP_WEBAUTHN_USER.APP_USER_ID, valueOf(userId))
                .set(APP_WEBAUTHN_USER.USER_HANDLE, userHandle)
                .set(APP_WEBAUTHN_USER.CREATION_TIMESTAMP, now)
                .set(APP_WEBAUTHN_USER.LAST_UPDATE_TIMESTAMP, now)
                .execute();
    }

    @Override
    public void insertCredential(PasskeyCredential credential) {
        ULong passkeyUserId = dslContext().select(APP_WEBAUTHN_USER.APP_WEBAUTHN_USER_ID)
                .from(APP_WEBAUTHN_USER)
                .where(APP_WEBAUTHN_USER.USER_HANDLE.eq(credential.userHandle()))
                .fetchOne(APP_WEBAUTHN_USER.APP_WEBAUTHN_USER_ID);
        if (passkeyUserId == null) {
            throw new IllegalStateException("The passkey user does not exist.");
        }

        LocalDateTime now = LocalDateTime.now();
        dslContext().insertInto(WEBAUTHN_CREDENTIAL)
                .set(WEBAUTHN_CREDENTIAL.APP_WEBAUTHN_USER_ID, passkeyUserId)
                .set(WEBAUTHN_CREDENTIAL.CREDENTIAL_ID, credential.credentialId())
                .set(WEBAUTHN_CREDENTIAL.RP_ID, credential.rpId())
                .set(WEBAUTHN_CREDENTIAL.PUBLIC_KEY, credential.publicKey())
                .set(WEBAUTHN_CREDENTIAL.SIGNATURE_COUNT, ULong.valueOf(credential.signatureCount()))
                .set(WEBAUTHN_CREDENTIAL.UV_INITIALIZED, flag(credential.uvInitialized()))
                .set(WEBAUTHN_CREDENTIAL.BACKUP_ELIGIBLE, flag(credential.backupEligible()))
                .set(WEBAUTHN_CREDENTIAL.BACKUP_STATE, flag(credential.backupState()))
                .set(WEBAUTHN_CREDENTIAL.TRANSPORTS, credential.transports())
                .set(WEBAUTHN_CREDENTIAL.CREDENTIAL_TYPE, credential.credentialType())
                .set(WEBAUTHN_CREDENTIAL.ATTESTATION_OBJECT, credential.attestationObject())
                .set(WEBAUTHN_CREDENTIAL.ATTESTATION_CLIENT_DATA_JSON, credential.attestationClientDataJson())
                .set(WEBAUTHN_CREDENTIAL.LABEL, credential.label())
                .set(WEBAUTHN_CREDENTIAL.CREATION_TIMESTAMP, now)
                .set(WEBAUTHN_CREDENTIAL.LAST_UPDATE_TIMESTAMP, now)
                .execute();
    }

    @Override
    public void updateAuthenticatorData(byte[] credentialId, long signatureCount,
                                        boolean uvInitialized, boolean backupEligible, boolean backupState,
                                        String userAgent) {
        LocalDateTime now = LocalDateTime.now();
        dslContext().update(WEBAUTHN_CREDENTIAL)
                .set(WEBAUTHN_CREDENTIAL.SIGNATURE_COUNT, ULong.valueOf(signatureCount))
                .set(WEBAUTHN_CREDENTIAL.UV_INITIALIZED, flag(uvInitialized))
                .set(WEBAUTHN_CREDENTIAL.BACKUP_ELIGIBLE, flag(backupEligible))
                .set(WEBAUTHN_CREDENTIAL.BACKUP_STATE, flag(backupState))
                .set(WEBAUTHN_CREDENTIAL.LAST_USED_TIMESTAMP, now)
                .set(WEBAUTHN_CREDENTIAL.LAST_USED_USER_AGENT, userAgent)
                .set(WEBAUTHN_CREDENTIAL.LAST_UPDATE_TIMESTAMP, now)
                .where(WEBAUTHN_CREDENTIAL.CREDENTIAL_ID.eq(credentialId))
                .execute();
    }

    @Override
    public int updateLabel(byte[] credentialId, String label) {
        return dslContext().update(WEBAUTHN_CREDENTIAL)
                .set(WEBAUTHN_CREDENTIAL.LABEL, label)
                .set(WEBAUTHN_CREDENTIAL.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(WEBAUTHN_CREDENTIAL.CREDENTIAL_ID.eq(credentialId))
                .execute();
    }

    @Override
    public int deleteCredential(byte[] credentialId) {
        return dslContext().deleteFrom(WEBAUTHN_CREDENTIAL)
                .where(WEBAUTHN_CREDENTIAL.CREDENTIAL_ID.eq(credentialId))
                .execute();
    }

    @Override
    public int deleteCredentialsByUserId(UserId userId) {
        return dslContext().deleteFrom(WEBAUTHN_CREDENTIAL)
                .where(WEBAUTHN_CREDENTIAL.APP_WEBAUTHN_USER_ID.in(
                        dslContext().select(APP_WEBAUTHN_USER.APP_WEBAUTHN_USER_ID)
                                .from(APP_WEBAUTHN_USER)
                                .where(APP_WEBAUTHN_USER.APP_USER_ID.eq(valueOf(userId)))))
                .execute();
    }

    @Override
    public int deleteCredentialsOutsideRpId(String rpId) {
        return dslContext().deleteFrom(WEBAUTHN_CREDENTIAL)
                .where(WEBAUTHN_CREDENTIAL.RP_ID.ne(rpId))
                .execute();
    }

    @Override
    public void deletePasskeyDataByUserId(UserId userId) {
        deleteCredentialsByUserId(userId);
        dslContext().deleteFrom(APP_WEBAUTHN_USER)
                .where(APP_WEBAUTHN_USER.APP_USER_ID.eq(valueOf(userId)))
                .execute();
    }

    @Override
    public void deletePasskeyUserByHandle(byte[] userHandle) {
        dslContext().deleteFrom(WEBAUTHN_CREDENTIAL)
                .where(WEBAUTHN_CREDENTIAL.APP_WEBAUTHN_USER_ID.in(
                        dslContext().select(APP_WEBAUTHN_USER.APP_WEBAUTHN_USER_ID)
                                .from(APP_WEBAUTHN_USER)
                                .where(APP_WEBAUTHN_USER.USER_HANDLE.eq(userHandle))))
                .execute();
        dslContext().deleteFrom(APP_WEBAUTHN_USER)
                .where(APP_WEBAUTHN_USER.USER_HANDLE.eq(userHandle))
                .execute();
    }

    private static Byte flag(boolean value) {
        return (byte) (value ? 1 : 0);
    }

}
