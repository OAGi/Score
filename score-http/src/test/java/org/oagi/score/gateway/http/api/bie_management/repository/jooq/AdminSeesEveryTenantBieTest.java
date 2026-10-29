package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListFilterCriteria;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

/**
 * In the multi-tenant mode a user sees the BIEs whose business contexts are connected to the user's tenants, and an Admin
 * sees the BIEs of every tenant, whether the Admin belongs to a tenant or not.
 */
class AdminSeesEveryTenantBieTest {

    private static final TenantId TENANT = new TenantId(BigInteger.valueOf(3));

    private static ScoreUser user(ScoreRole... roles) {
        return new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester", "tester@example.com",
                roles.length > 0 && List.of(roles).contains(ScoreRole.DEVELOPER), List.of(roles));
    }

    private List<String> sql = new ArrayList<>();

    private JooqBieQueryRepository repository(ScoreUser requester) {
        sql = new ArrayList<>();
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        return new JooqBieQueryRepository(DSL.using(new MockConnection(provider), SQLDialect.MYSQL),
                requester, mock(RepositoryFactory.class, RETURNS_DEEP_STUBS));
    }

    private boolean listFiltersByTenant(ScoreUser requester) {
        var repo = repository(requester);
        try {
            repo.getBieList(BieListFilterCriteria.builder(new LibraryId(BigInteger.ONE), List.of(new ReleaseId(BigInteger.ONE)))
                    .tenantEnabled(true).userTenantIdList(List.of(TENANT)).build(), new PageRequest(-1, -1, List.of()));
        } catch (RuntimeException ignored) {
            // The mocked connection returns no rows; only the statements it received matter here.
        }
        return sql.stream().anyMatch(s -> s.contains("`biz_ctx`.`biz_ctx_id` in (select `tenant_business_ctx`.`biz_ctx_id`"));
    }

    private boolean summaryFiltersByTenant(ScoreUser requester) {
        var repo = repository(requester);
        try {
            repo.getSummaryBieList(new LibraryId(BigInteger.ONE), null, true, List.of(TENANT));
        } catch (RuntimeException ignored) {
            // The mocked connection returns no rows; only the statements it received matter here.
        }
        return sql.stream().anyMatch(s -> s.contains("`tenant_business_ctx`"));
    }

    @Test
    void theBieListOfAnEndUserOrADeveloperIsFilteredByTheirTenants() {
        assertTrue(listFiltersByTenant(user(ScoreRole.END_USER)), sql.toString());
        assertTrue(listFiltersByTenant(user(ScoreRole.DEVELOPER)), sql.toString());
    }

    @Test
    void theBieListOfAnAdminIsNotFilteredByTenant() {
        assertFalse(listFiltersByTenant(user(ScoreRole.ADMINISTRATOR, ScoreRole.END_USER)), sql.toString());
        assertFalse(listFiltersByTenant(user(ScoreRole.ADMINISTRATOR, ScoreRole.DEVELOPER)), sql.toString());
    }

    @Test
    void theBieSummaryOfAnEndUserIsFilteredByTheirTenantsAndAnAdminsIsNot() {
        assertTrue(summaryFiltersByTenant(user(ScoreRole.END_USER)), sql.toString());
        assertFalse(summaryFiltersByTenant(user(ScoreRole.ADMINISTRATOR, ScoreRole.END_USER)), sql.toString());
    }

}
