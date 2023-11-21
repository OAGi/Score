package org.oagi.score.gateway.http.api.account_management.service;

import org.jooq.DSLContext;
import org.jooq.impl.TableImpl;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppUserRecord;
import org.oagi.score.service.common.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.oagi.score.repo.api.impl.jooq.entity.tables.AppUser.APP_USER;
import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.ReflectionUtils.handleReflectionException;


@Service
public class AccountService {

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Transactional
    public void setEnable(AuthenticatedPrincipal user, long targetUserId, boolean enabled) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        if (!requester.isDeveloper()) {
            throw new InsufficientAuthenticationException(
                    messages.getMessage(
                            "ExceptionTranslationFilter.insufficientAuthentication",
                            "Full authentication is required to access this resource"));
        }

        AppUserRecord targetAppUser = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(targetUserId)))
                .fetchOptional().orElse(null);
        if (targetAppUser == null) {
            throw new IllegalArgumentException();
        }

        boolean prevEnabled = (targetAppUser.getIsEnabled() == (byte) 1);
        if (prevEnabled != enabled) {
            dslContext.update(APP_USER)
                    .set(APP_USER.IS_ENABLED, (byte) ((enabled) ? 1 : 0))
                    .where(APP_USER.APP_USER_ID.eq(targetAppUser.getAppUserId()))
                    .execute();
        }

        if (!enabled) {
            sessionService.invalidateByUsername(targetAppUser.getLoginId());
        }
    }

    @Transactional
    public void transferOwnership(AuthenticatedPrincipal user, BigInteger targetUserId) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        if (!requester.isAdmin()) {
            throw new InsufficientAuthenticationException(
                    messages.getMessage(
                            "ExceptionTranslationFilter.insufficientAuthentication",
                            "Full authentication is required to access this resource"));
        }

        AppUserRecord targetAppUser = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(targetUserId)))
                .fetchOptional().orElse(null);
        if (targetAppUser == null) {
            throw new IllegalArgumentException();
        }

        doWithDeclaredFields(Tables.class, field -> {
            TableImpl<?> tableImpl = (TableImpl<?>) field.getObject();
            boolean hasOwnerUserId;
            try {
                hasOwnerUserId = tableImpl.getClass().getDeclaredField("OWNER_USER_ID") != null;
            } catch (NoSuchFieldException e) {
                return;
            }

            if (hasOwnerUserId) {
                dslContext.execute("UPDATE `" + tableImpl.getName() + "` SET `owner_user_id` = ? WHERE `owner_user_id` = ?",
                        ULong.valueOf(requester.getAppUserId()), targetAppUser.getAppUserId());
            }
        }, field -> field.getObject() != null && field.getObject() instanceof TableImpl<?>);
    }

    private void doWithDeclaredFields(Class<?> clazz, Consumer<FieldContainer> callback, Predicate<FieldContainer> filter) {
        for (Field field : clazz.getDeclaredFields()) {
            Object obj = getField(field, clazz);
            FieldContainer container = new FieldContainer() {
                @Override
                public Field getField() {
                    return field;
                }
                @Override
                public Object getObject() {
                    return obj;
                }
            };

            if (filter.test(container)) {
                callback.accept(container);
            }
        }
    }

    private interface FieldContainer {

        public Field getField();

        public Object getObject();

    }

}
