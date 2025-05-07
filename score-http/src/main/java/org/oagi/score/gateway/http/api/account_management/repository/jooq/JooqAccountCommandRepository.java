package org.oagi.score.gateway.http.api.account_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.TableImpl;
import org.oagi.score.gateway.http.api.account_management.model.OAuth2UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.repository.AccountCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Tables;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AppUser;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AppUserRecord;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.APP_OAUTH2_USER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.APP_USER;
import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAccountCommandRepository extends JooqBaseRepository implements AccountCommandRepository {

    private final PasswordEncoder passwordEncoder;

    public JooqAccountCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory,
                                        PasswordEncoder passwordEncoder) {
        super(dslContext, requester, repositoryFactory);

        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserId create(String loginId, String password, String name, String organization, boolean developer, boolean admin) {

        AppUserRecord record = new AppUserRecord();
        record.setLoginId(loginId);
        if (hasLength(password)) {
            record.setPassword(passwordEncoder.encode(password));
        }
        record.setName(name);
        record.setOrganization(organization);
        record.setIsDeveloper((byte) (developer ? 1 : 0));
        record.setIsAdmin((byte) (admin ? 1 : 0));
        record.setIsEnabled((byte) 1);

        return new UserId(dslContext().insertInto(APP_USER)
                .set(record)
                .returning(APP_USER.APP_USER_ID)
                .fetchOne().getAppUserId().toBigInteger());
    }

    @Override
    public boolean update(UserId userId, String username, String organization, boolean admin, String newPassword) {

        UpdateSetMoreStep step = dslContext().update(APP_USER)
                .set(APP_USER.NAME, username)
                .set(APP_USER.ORGANIZATION, organization)
                .set(APP_USER.IS_ADMIN, (byte) (admin ? 1 : 0));
        if (hasLength(newPassword)) {
            step = step.set(APP_USER.PASSWORD, passwordEncoder.encode(newPassword));
        }

        int numOfUpdatedRecords = step
                .where(APP_USER.APP_USER_ID.equal(valueOf(userId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updatePassword(String password) {

        int numOfUpdatedRecords = dslContext().update(APP_USER)
                .set(APP_USER.PASSWORD, passwordEncoder.encode(password))
                .where(APP_USER.APP_USER_ID.eq(valueOf(requester().userId())))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean linkOAuth2User(OAuth2UserId oAuth2UserId, UserId userId) {

        int numOfUpdatedRecords = dslContext().update(APP_OAUTH2_USER)
                .set(APP_OAUTH2_USER.APP_USER_ID, valueOf(userId))
                .where(APP_OAUTH2_USER.APP_OAUTH2_USER_ID.eq(valueOf(oAuth2UserId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delinkOAuth2User(UserId userId) {

        int numOfUpdatedRecords = dslContext().update(APP_OAUTH2_USER)
                .setNull(APP_OAUTH2_USER.APP_USER_ID)
                .where(APP_OAUTH2_USER.APP_USER_ID.eq(valueOf(userId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateEmail(String email) {

        int numOfUpdatedRecords = dslContext().update(APP_USER)
                .set(APP_USER.EMAIL, email)
                .set(APP_USER.EMAIL_VERIFIED, (byte) 0)
                .setNull(APP_USER.EMAIL_VERIFIED_TIMESTAMP)
                .where(APP_USER.APP_USER_ID.eq(valueOf(requester().userId())))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public void setEmailVerified(UserId userId, boolean emailVerified) {

        dslContext().update(APP_USER)
                .set(APP_USER.EMAIL_VERIFIED, (byte) (emailVerified ? 1 : 0))
                .set(APP_USER.EMAIL_VERIFIED_TIMESTAMP, LocalDateTime.now())
                .where(APP_USER.APP_USER_ID.eq(valueOf(userId)))
                .execute();
    }

    @Override
    public boolean delete(UserId userId) {

        dslContext().deleteFrom(APP_OAUTH2_USER)
                .where(APP_OAUTH2_USER.APP_USER_ID.eq(valueOf(userId)))
                .execute();
        int numOfDeletedRecords = dslContext().deleteFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(valueOf(userId)))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public boolean setEnabled(UserId userId, boolean enabled) {

        int numOfUpdatedRecords = dslContext().update(AppUser.APP_USER)
                .set(AppUser.APP_USER.IS_ENABLED, (byte) ((enabled) ? 1 : 0))
                .where(AppUser.APP_USER.APP_USER_ID.eq(valueOf(userId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public void updateOwnerUser(UserId userId) {

        doWithDeclaredFields(Tables.class, field -> {
            TableImpl<?> tableImpl = (TableImpl<?>) field.object();
            boolean hasOwnerUserId;
            try {
                hasOwnerUserId = tableImpl.getClass().getDeclaredField("OWNER_USER_ID") != null;
            } catch (NoSuchFieldException e) {
                return;
            }

            if (hasOwnerUserId) {
                dslContext().execute("UPDATE `" + tableImpl.getName() + "` SET `owner_user_id` = ? WHERE `owner_user_id` = ?",
                        valueOf(requester().userId()), valueOf(userId));
            }
        }, field -> field.object() != null && field.object() instanceof TableImpl<?>);
    }

    private void doWithDeclaredFields(Class<?> clazz, Consumer<FieldContainer> callback, Predicate<FieldContainer> filter) {
        for (Field field : clazz.getDeclaredFields()) {
            Object obj = getField(field, clazz);
            FieldContainer container = new FieldContainer(field, obj);
            if (filter.test(container)) {
                callback.accept(container);
            }
        }
    }

    private record FieldContainer(Field field, Object object) {
    }
}
