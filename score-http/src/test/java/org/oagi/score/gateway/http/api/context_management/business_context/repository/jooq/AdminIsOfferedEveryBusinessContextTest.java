package org.oagi.score.gateway.http.api.context_management.business_context.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria.BusinessContextListFilterCriteria;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * In the multi-tenant mode a user is offered, to create a BIE, the business contexts of the user's tenants, and an Admin
 * is offered the business contexts of every tenant.
 */
class AdminIsOfferedEveryBusinessContextTest {

    private final List<String> sql = new ArrayList<>();

    private boolean bieEditingListFiltersByTenant(ScoreUser requester) {
        sql.clear();
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        var repo = new JooqBusinessContextQueryRepository(DSL.using(new MockConnection(provider), SQLDialect.MYSQL),
                requester, mock(RepositoryFactory.class, RETURNS_DEEP_STUBS));
        try {
            repo.getBusinessContextList(new BusinessContextListFilterCriteria(null, true, null, false, true,
                    List.of(new TenantId(BigInteger.valueOf(3))), List.of(), null), new PageRequest(-1, -1, List.of()));
        } catch (RuntimeException ignored) {
            // The mocked connection returns no rows; only the statements it received matter here.
        }
        return sql.stream().anyMatch(s -> s.contains("`biz_ctx`.`biz_ctx_id` in (select `tenant_business_ctx`.`biz_ctx_id`"));
    }

    private static ScoreUser user(ScoreRole... roles) {
        return new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester", "tester@example.com", false, List.of(roles));
    }

    @Test
    void anEndUserIsOfferedTheBusinessContextsOfTheirTenantsOnly() {
        assertTrue(bieEditingListFiltersByTenant(user(ScoreRole.END_USER)), sql.toString());
    }

    @Test
    void anAdminIsOfferedEveryBusinessContext() {
        assertFalse(bieEditingListFiltersByTenant(user(ScoreRole.ADMINISTRATOR, ScoreRole.END_USER)), sql.toString());
    }

}
