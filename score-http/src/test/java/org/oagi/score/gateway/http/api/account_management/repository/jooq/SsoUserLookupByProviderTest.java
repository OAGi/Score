package org.oagi.score.gateway.http.api.account_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/** An SSO account is looked up by the identity provider and the sub, as the sub is unique within one provider only. */
class SsoUserLookupByProviderTest {

    private final List<String> sql = new ArrayList<>();
    private final List<List<Object>> bindings = new ArrayList<>();

    private JooqScoreUserQueryRepository userRepository() {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            bindings.add(Arrays.asList(ctx.bindings()));
            if (ctx.sql().toLowerCase(Locale.ROOT).startsWith("select exists")) {
                var exists = DSL.field("exists", Boolean.class);
                var result = DSL.using(SQLDialect.MYSQL).newResult(exists);
                result.add(DSL.using(SQLDialect.MYSQL).newRecord(exists).values(true));
                return new MockResult[]{new MockResult(1, result)};
            }
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        return new JooqScoreUserQueryRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), mock(RepositoryFactory.class));
    }

    @Test
    void theScoreUserLookupIsRestrictedToTheProvider() {
        userRepository().getScoreUserByOidcSub("provider-a", "1001");

        assertTrue(sql.get(0).contains("`oauth2_app`.`provider_name` = ?"), sql.toString());
        assertTrue(bindings.get(0).contains("provider-a") && bindings.get(0).contains("1001"), bindings.toString());
    }

    @Test
    void withoutAProviderTheLookupStaysOnTheSub() {
        userRepository().getScoreUserByOidcSub(null, "1001");

        assertTrue(!sql.get(0).contains("provider_name"), sql.toString());
    }

    @Test
    void aLinkedAccountThatIsNotEnabledIsDisabledForThePasswordAndTheSsoLookup() {
        userRepository().isAccountDisabled("alice");
        userRepository().isOidcAccountDisabled("provider-a", "1001");

        assertTrue(sql.get(0).contains("`app_user`.`is_enabled` is null or `app_user`.`is_enabled` <> ?"), sql.toString());
        assertTrue(sql.get(1).contains("`app_user`.`is_enabled` is null or `app_user`.`is_enabled` <> ?")
                && sql.get(1).contains("`oauth2_app`.`provider_name` = ?"), sql.toString());
    }

    @Test
    void theSubsOfAnAccountAreReadThroughItsLoginId() {
        userRepository().getOidcSubs("alice");

        assertTrue(sql.get(0).contains("`app_oauth2_user`.`sub`") && sql.get(0).contains("`app_user`.`login_id`"), sql.toString());
    }
}
