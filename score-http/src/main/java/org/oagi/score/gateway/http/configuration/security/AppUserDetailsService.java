package org.oagi.score.gateway.http.configuration.security;

import org.jooq.DSLContext;
import org.oagi.score.entity.jooq.Tables;
import org.oagi.score.entity.jooq.tables.records.AppUserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AppUserDetailsService implements UserDetailsService {

    public static String DEVELOPER_GRANTED_AUTHORITY = "developer";
    public static String END_USER_GRANTED_AUTHORITY = "end-user";
    public static String PENDING_GRANTED_AUTHORITY = "pending";
    public static String REJECT_GRANTED_AUTHORITY = "reject";

    @Autowired
    private DSLContext dslContext;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUserRecord appUserRecord = dslContext.selectFrom(Tables.APP_USER)
                .where(Tables.APP_USER.LOGIN_ID.equalIgnoreCase(username))
                .fetchOptional().orElse(null);
        if (appUserRecord == null) {
            throw new UsernameNotFoundException(username);
        }

        username = appUserRecord.getLoginId();
        String password = appUserRecord.getPassword();
        boolean isDeveloper = appUserRecord.getIsDeveloper() == 1;

        return new ScoreUser(username, password,
                Arrays.asList(new SimpleGrantedAuthority((isDeveloper) ? DEVELOPER_GRANTED_AUTHORITY : END_USER_GRANTED_AUTHORITY)));
    }
}
