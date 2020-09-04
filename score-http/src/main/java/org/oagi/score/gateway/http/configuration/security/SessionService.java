package org.oagi.score.gateway.http.configuration.security;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.oagi.score.entity.jooq.Tables.APP_OAUTH2_USER;
import static org.oagi.score.entity.jooq.Tables.APP_USER;

@Service
@Transactional(readOnly = true)
public class SessionService {

    @Autowired
    private DSLContext dslContext;

    public long userId(User user) {
        if (user == null) {
            return 0L;
        }
        return dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(user.getUsername()))
                .fetchOptional(APP_USER.APP_USER_ID).orElse(ULong.valueOf(0L)).longValue();
    }

    public long userId(OAuth2User user) {
        if (user == null) {
            return 0L;
        }
        return dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .join(APP_OAUTH2_USER).on(APP_USER.APP_USER_ID.eq(APP_OAUTH2_USER.APP_USER_ID))
                .where(APP_OAUTH2_USER.SUB.equalIgnoreCase((String) user.getAttribute("sub")))
                .fetchOptional(APP_USER.APP_USER_ID).orElse(ULong.valueOf(0L)).longValue();
    }

    public long userId(AuthenticatedPrincipal principal) {
        if (principal instanceof User) {
            return userId((User) principal);
        } else if (principal instanceof OAuth2User) {
            return userId((OAuth2User) principal);
        } else {
            return 0L;
        }
    }

    public AppUser getAppUser(String username) {
        return dslContext.select(
                APP_USER.APP_USER_ID,
                APP_USER.LOGIN_ID,
                APP_USER.NAME,
                APP_USER.IS_DEVELOPER.as("developer"),
                APP_USER.ORGANIZATION)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(username))
                .fetchOneInto(AppUser.class);
    }

    public AppUser getAppUser(User user) {
        return getAppUser(user.getUsername());
    }

    public AppUser getAppUser(OAuth2User user) {
        String sub = user.getAttribute("sub");
        return dslContext.select(
                APP_USER.APP_USER_ID,
                APP_USER.LOGIN_ID,
                APP_USER.NAME,
                APP_USER.IS_DEVELOPER.as("developer"),
                APP_USER.ORGANIZATION)
                .from(APP_USER)
                .join(APP_OAUTH2_USER).on(APP_USER.APP_USER_ID.eq(APP_OAUTH2_USER.APP_USER_ID))
                .where(APP_OAUTH2_USER.SUB.equalIgnoreCase(sub))
                .fetchOneInto(AppUser.class);
    }

    public AppUser getAppUser(AuthenticatedPrincipal user) {
        if (user instanceof User) {
            return getAppUser((User) user);
        } else if (user instanceof OAuth2User) {
            return getAppUser((OAuth2User) user);
        }
        return getAppUser(user.getName());
    }
}
