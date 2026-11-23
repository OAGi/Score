package org.oagi.score.gateway.http.configuration.security;

import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Tables;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AppUserRecord;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The loaded user carries the enabled flag of the account, so a disabled account cannot sign in. */
class AppUserDetailsServiceTest {

    private AppUserDetailsService serviceWith(Byte isEnabled) {
        Result<AppUserRecord> result = DSL.using(SQLDialect.MYSQL).newResult(Tables.APP_USER);
        AppUserRecord record = DSL.using(SQLDialect.MYSQL).newRecord(Tables.APP_USER);
        record.setLoginId("alice");
        record.setPassword("secret");
        record.setIsDeveloper((byte) 0);
        record.setIsAdmin((byte) 0);
        record.setIsEnabled(isEnabled);
        result.add(record);
        AppUserDetailsService service = new AppUserDetailsService();
        ReflectionTestUtils.setField(service, "dslContext",
                DSL.using(new MockConnection(ctx -> new MockResult[]{new MockResult(1, result)}), SQLDialect.MYSQL));
        return service;
    }

    @Test
    void enabledAccountIsEnabled() {
        ScoreUserDetails user = (ScoreUserDetails) serviceWith((byte) 1).loadUserByUsername("alice");
        assertTrue(user.isEnabled());
        new AccountStatusUserDetailsChecker().check(user);
    }

    @Test
    void disabledAccountIsRejectedByThePreAuthenticationCheck() {
        ScoreUserDetails user = (ScoreUserDetails) serviceWith((byte) 0).loadUserByUsername("alice");
        assertFalse(user.isEnabled());
        assertThrows(DisabledException.class, () -> new AccountStatusUserDetailsChecker().check(user));
    }

    @Test
    void accountWithoutTheFlagIsNotEnabled() {
        assertFalse(serviceWith(null).loadUserByUsername("alice").isEnabled());
    }

    @Test
    void unknownAccountStillFails() {
        AppUserDetailsService service = new AppUserDetailsService();
        ReflectionTestUtils.setField(service, "dslContext", DSL.using(new MockConnection(
                ctx -> new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult(Tables.APP_USER))}), SQLDialect.MYSQL));
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nobody"));
    }
}
