package org.oagi.srt.gateway.http.configuration.security;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.oagi.srt.entity.jooq.Tables.APP_USER;

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

    public AppUser getAppUser(String username) {
        return dslContext.select(
                APP_USER.APP_USER_ID,
                APP_USER.LOGIN_ID,
                APP_USER.NAME,
                APP_USER.IS_DEVELOPER.as("developer"),
                APP_USER.ORGANIZATION
        ).from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(username))
                .fetchOneInto(AppUser.class);
    }

    public AppUser getAppUser(User user) {
        return getAppUser(user.getUsername());
    }
}
