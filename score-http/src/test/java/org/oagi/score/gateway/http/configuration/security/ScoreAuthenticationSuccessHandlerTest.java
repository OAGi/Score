package org.oagi.score.gateway.http.configuration.security;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AppOauth2UserRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.Oauth2AppRecord;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import org.jooq.types.ULong;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAUTH2_APP;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AppOauth2User.APP_OAUTH2_USER;

/** An SSO login is bound to the enabled OAuth2 app of the provider that authenticated the user. */
class ScoreAuthenticationSuccessHandlerTest {

    private static final String PROVIDER = "auth0";

    private final DSLContext create = DSL.using(SQLDialect.MYSQL);
    private final List<String> sql = new ArrayList<>();
    private final List<List<Object>> bindings = new ArrayList<>();

    private ScoreAuthenticationSuccessHandler handler(boolean appExists, boolean userExists) {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            String statement = ctx.sql().toLowerCase(Locale.ROOT);
            sql.add(statement);
            bindings.add(Arrays.asList(ctx.bindings()));

            if (statement.startsWith("insert")) {
                Result<Record1<ULong>> keys = create.newResult(APP_OAUTH2_USER.APP_OAUTH2_USER_ID);
                keys.add(create.newRecord(APP_OAUTH2_USER.APP_OAUTH2_USER_ID).value1(ULong.valueOf(7)));
                return new MockResult[]{new MockResult(1, keys)};
            }
            if (statement.contains("from `oauth2_app`")) {
                Result<Oauth2AppRecord> result = create.newResult(OAUTH2_APP);
                if (appExists) {
                    Oauth2AppRecord record = create.newRecord(OAUTH2_APP);
                    record.setOauth2AppId(ULong.valueOf(3));
                    record.setProviderName(PROVIDER);
                    result.add(record);
                }
                return new MockResult[]{new MockResult(result.size(), result)};
            }
            Result<AppOauth2UserRecord> result = create.newResult(APP_OAUTH2_USER);
            if (userExists) {
                AppOauth2UserRecord record = create.newRecord(APP_OAUTH2_USER);
                record.setAppOauth2UserId(ULong.valueOf(9));
                result.add(record);
            }
            return new MockResult[]{new MockResult(result.size(), result)};
        };
        ScoreAuthenticationSuccessHandler handler = new ScoreAuthenticationSuccessHandler();
        ReflectionTestUtils.setField(handler, "dslContext",
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL));
        return handler;
    }

    private OAuth2AuthenticationToken token(String registrationId) {
        OAuth2User user = new DefaultOAuth2User(AuthorityUtils.createAuthorityList("ROLE_USER"),
                Map.of("sub", "auth0|1001", "name", "Kim"), "sub");
        return new OAuth2AuthenticationToken(user, user.getAuthorities(), registrationId);
    }

    private OAuth2User principalOf(OAuth2AuthenticationToken token) {
        return (OAuth2User) token.getPrincipal();
    }

    @Test
    void theAppLookupIsRestrictedToTheEnabledAppOfTheProvider() {
        OAuth2AuthenticationToken token = token(PROVIDER);

        handler(true, true).storeOAuth2UserInfoIfAbsent(token, principalOf(token));

        assertTrue(sql.get(0).contains("`oauth2_app`.`provider_name` = ?"), sql.toString());
        assertTrue(sql.get(0).contains("`oauth2_app`.`is_disabled` = ?"), sql.toString());
        assertTrue(bindings.get(0).contains(PROVIDER), bindings.toString());
    }

    @Test
    void anUnknownProviderIsReportedByName() {
        OAuth2AuthenticationToken token = token("gone");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> handler(false, false).storeOAuth2UserInfoIfAbsent(token, principalOf(token)));

        assertTrue(e.getMessage().contains("'gone'"), e.getMessage());
        assertEquals(1, sql.size(), "no user lookup nor insert after the failed app lookup: " + sql);
    }

    @Test
    void anExistingUserIsLookedUpByTheAppAndTheSubAndIsNotStoredAgain() {
        OAuth2AuthenticationToken token = token(PROVIDER);

        AppOauth2UserRecord stored = handler(true, true).storeOAuth2UserInfoIfAbsent(token, principalOf(token));

        assertNull(stored);
        assertTrue(sql.get(1).contains("`app_oauth2_user`.`oauth2_app_id` = ?")
                && sql.get(1).contains("`app_oauth2_user`.`sub` = ?"), sql.toString());
        assertEquals(2, sql.size(), sql.toString());
    }

    @Test
    void aNewUserIsStoredUnderTheAppOfTheProvider() {
        OAuth2AuthenticationToken token = token(PROVIDER);

        AppOauth2UserRecord stored = handler(true, false).storeOAuth2UserInfoIfAbsent(token, principalOf(token));

        assertNotNull(stored);
        assertEquals(ULong.valueOf(3), stored.getOauth2AppId());
        assertEquals("auth0|1001", stored.getSub());
        assertTrue(sql.get(2).startsWith("insert into `app_oauth2_user`"), sql.toString());
    }
}
