package org.oagi.score.gateway.http.api.passkey_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyUser;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.List;
import java.util.Optional;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqPasskeyQueryRepository extends JooqBaseRepository implements PasskeyQueryRepository {

    public JooqPasskeyQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public Optional<UserId> findUserIdByLoginId(String loginId) {
        return dslContext().select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(loginId))
                .fetchOptional(APP_USER.APP_USER_ID)
                .map(e -> new UserId(e.toBigInteger()));
    }

    @Override
    public Optional<String> findLoginId(UserId userId) {
        return dslContext().select(APP_USER.LOGIN_ID)
                .from(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(valueOf(userId)))
                .fetchOptional(APP_USER.LOGIN_ID);
    }

    @Override
    public Optional<PasskeyUser> findPasskeyUserByHandle(byte[] userHandle) {
        return dslContext().select(APP_USER.APP_USER_ID, APP_USER.LOGIN_ID, APP_USER.NAME, APP_WEBAUTHN_USER.USER_HANDLE)
                .from(APP_WEBAUTHN_USER)
                .join(APP_USER).on(APP_USER.APP_USER_ID.eq(APP_WEBAUTHN_USER.APP_USER_ID))
                .where(APP_WEBAUTHN_USER.USER_HANDLE.eq(userHandle))
                .fetchOptional(this::toPasskeyUser);
    }

    @Override
    public Optional<PasskeyUser> findPasskeyUserByLoginId(String loginId) {
        return dslContext().select(APP_USER.APP_USER_ID, APP_USER.LOGIN_ID, APP_USER.NAME, APP_WEBAUTHN_USER.USER_HANDLE)
                .from(APP_WEBAUTHN_USER)
                .join(APP_USER).on(APP_USER.APP_USER_ID.eq(APP_WEBAUTHN_USER.APP_USER_ID))
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(loginId))
                .orderBy(APP_WEBAUTHN_USER.APP_WEBAUTHN_USER_ID.asc())
                .limit(1)
                .fetchOptional(this::toPasskeyUser);
    }

    private PasskeyUser toPasskeyUser(Record record) {
        String name = record.get(APP_USER.NAME);
        String loginId = record.get(APP_USER.LOGIN_ID);
        return new PasskeyUser(
                new UserId(record.get(APP_USER.APP_USER_ID).toBigInteger()),
                loginId,
                (name != null && !name.isBlank()) ? name : loginId,
                record.get(APP_WEBAUTHN_USER.USER_HANDLE));
    }

    @Override
    public Optional<PasskeyCredential> findCredential(byte[] credentialId) {
        return credentialSelect()
                .where(WEBAUTHN_CREDENTIAL.CREDENTIAL_ID.eq(credentialId))
                .fetchOptional(this::toCredential);
    }

    @Override
    public List<PasskeyCredential> findCredentialsByUserHandle(byte[] userHandle) {
        return credentialSelect()
                .where(APP_WEBAUTHN_USER.USER_HANDLE.eq(userHandle))
                .orderBy(WEBAUTHN_CREDENTIAL.WEBAUTHN_CREDENTIAL_ID.asc())
                .fetch(this::toCredential);
    }

    @Override
    public List<PasskeyCredential> findCredentialsByUserId(UserId userId) {
        return credentialSelect()
                .where(APP_WEBAUTHN_USER.APP_USER_ID.eq(valueOf(userId)))
                .orderBy(WEBAUTHN_CREDENTIAL.WEBAUTHN_CREDENTIAL_ID.asc())
                .fetch(this::toCredential);
    }

    @Override
    public int countCredentialsByUserId(UserId userId) {
        return dslContext().selectCount()
                .from(WEBAUTHN_CREDENTIAL)
                .join(APP_WEBAUTHN_USER).on(APP_WEBAUTHN_USER.APP_WEBAUTHN_USER_ID.eq(WEBAUTHN_CREDENTIAL.APP_WEBAUTHN_USER_ID))
                .where(APP_WEBAUTHN_USER.APP_USER_ID.eq(valueOf(userId)))
                .fetchOne(0, int.class);
    }

    @Override
    public boolean hasPassword(UserId userId) {
        String password = dslContext().select(APP_USER.PASSWORD)
                .from(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(valueOf(userId)))
                .fetchOne(APP_USER.PASSWORD);
        return password != null && !password.isBlank();
    }

    private org.jooq.SelectOnConditionStep<Record> credentialSelect() {
        return dslContext().select(WEBAUTHN_CREDENTIAL.fields())
                .select(APP_WEBAUTHN_USER.USER_HANDLE, APP_WEBAUTHN_USER.APP_USER_ID)
                .from(WEBAUTHN_CREDENTIAL)
                .join(APP_WEBAUTHN_USER).on(APP_WEBAUTHN_USER.APP_WEBAUTHN_USER_ID.eq(WEBAUTHN_CREDENTIAL.APP_WEBAUTHN_USER_ID));
    }

    private PasskeyCredential toCredential(Record record) {
        ULong signatureCount = record.get(WEBAUTHN_CREDENTIAL.SIGNATURE_COUNT);
        return new PasskeyCredential(
                record.get(WEBAUTHN_CREDENTIAL.CREDENTIAL_ID),
                record.get(APP_WEBAUTHN_USER.USER_HANDLE),
                new UserId(record.get(APP_WEBAUTHN_USER.APP_USER_ID).toBigInteger()),
                record.get(WEBAUTHN_CREDENTIAL.RP_ID),
                record.get(WEBAUTHN_CREDENTIAL.PUBLIC_KEY),
                (signatureCount != null) ? signatureCount.longValue() : 0L,
                flag(record.get(WEBAUTHN_CREDENTIAL.UV_INITIALIZED)),
                flag(record.get(WEBAUTHN_CREDENTIAL.BACKUP_ELIGIBLE)),
                flag(record.get(WEBAUTHN_CREDENTIAL.BACKUP_STATE)),
                record.get(WEBAUTHN_CREDENTIAL.TRANSPORTS),
                record.get(WEBAUTHN_CREDENTIAL.CREDENTIAL_TYPE),
                record.get(WEBAUTHN_CREDENTIAL.ATTESTATION_OBJECT),
                record.get(WEBAUTHN_CREDENTIAL.ATTESTATION_CLIENT_DATA_JSON),
                record.get(WEBAUTHN_CREDENTIAL.LABEL),
                record.get(WEBAUTHN_CREDENTIAL.CREATION_TIMESTAMP),
                record.get(WEBAUTHN_CREDENTIAL.LAST_UPDATE_TIMESTAMP),
                record.get(WEBAUTHN_CREDENTIAL.LAST_USED_TIMESTAMP),
                record.get(WEBAUTHN_CREDENTIAL.LAST_USED_USER_AGENT));
    }

    private static boolean flag(Byte value) {
        return value != null && value != 0;
    }

}
