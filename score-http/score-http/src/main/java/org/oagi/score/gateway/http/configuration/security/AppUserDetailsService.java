package org.oagi.score.gateway.http.configuration.security;

import org.jooq.DSLContext;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AppUserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.service.configuration.AppUserAuthority.*;

@Component
public class AppUserDetailsService implements UserDetailsService {

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
        boolean isAdmin = appUserRecord.getIsAdmin() == 1;

        List<GrantedAuthority> authorities = new ArrayList();
        authorities.add(new SimpleGrantedAuthority((isDeveloper) ? DEVELOPER_GRANTED_AUTHORITY : END_USER_GRANTED_AUTHORITY));
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority(ADMIN_GRANTED_AUTHORITY));
        }
        return new ScoreUser(username, password, authorities);
    }
}
