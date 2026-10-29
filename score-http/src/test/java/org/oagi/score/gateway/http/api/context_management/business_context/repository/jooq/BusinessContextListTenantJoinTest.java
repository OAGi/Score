package org.oagi.score.gateway.http.api.context_management.business_context.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria.BusinessContextListFilterCriteria;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.PageRequest;
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
 * A business context connected to several tenants must be listed (and counted) once, so the
 * list may not join {@code tenant_business_ctx}; tenant conditions are sub-selects instead.
 */
class BusinessContextListTenantJoinTest {

    private List<String> listSql(BusinessContextListFilterCriteria criteria) {
        List<String> sql = new ArrayList<>();
        var create = DSL.using(SQLDialect.MYSQL);
        JooqBusinessContextQueryRepository repo = new JooqBusinessContextQueryRepository(
                DSL.using(new MockConnection(ctx -> {
                    sql.add(ctx.sql().toLowerCase(Locale.ROOT));
                    var count = org.jooq.impl.DSL.field("c", Integer.class);
                    var result = create.newResult(count);
                    var record = create.newRecord(count);
                    record.value1(0);
                    result.add(record);
                    return new MockResult[]{new MockResult(1, result)};
                }), SQLDialect.MYSQL), null, mock(RepositoryFactory.class, RETURNS_DEEP_STUBS));
        try {
            repo.getBusinessContextList(criteria, new PageRequest(0, 10, List.of()));
        } catch (RuntimeException ignored) {
            // mapping the mocked rows is not under test; the issued statements are
        }
        return sql;
    }

    @Test
    void tenantFilterIsASubSelectNotAJoin() {
        List<String> sql = listSql(new BusinessContextListFilterCriteria(null,
                true, new TenantId(BigInteger.valueOf(3)), false, false, List.of(), List.of(), null));

        assertFalse(sql.isEmpty());
        for (String s : sql) {
            assertFalse(s.contains("join `tenant_business_ctx`"), s);
        }
        assertTrue(sql.stream().anyMatch(s -> s.contains("`biz_ctx`.`biz_ctx_id` in (select `tenant_business_ctx`.`biz_ctx_id`")),
                sql.toString());
    }

    @Test
    void bieEditingFilterDoesNotJoinTenants() {
        List<String> sql = listSql(new BusinessContextListFilterCriteria(null,
                true, null, false, true, List.of(new TenantId(BigInteger.ONE), new TenantId(BigInteger.TWO)), List.of(), null));

        for (String s : sql) {
            assertFalse(s.contains("join `tenant_business_ctx`"), s);
        }
    }
}
