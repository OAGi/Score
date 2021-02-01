package org.oagi.score.gateway.http.api.account_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppUserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.oagi.score.repo.api.impl.jooq.entity.tables.AppUser.APP_USER;


@Service
public class AccountService {

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Transactional
    public void setEnable(AuthenticatedPrincipal user, long targetUserId, boolean enabled) {
        AppUser requester = sessionService.getAppUser(user);
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

}
