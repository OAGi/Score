package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.repository.criteria.ModuleSetReleaseListFilterCriteria;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * The Updated date range of the Module Set Release list filters on the Module Set Release's own timestamp,
 * the one that the Updated on column shows, and not on the Module Set's.
 */
class ModuleSetReleaseUpdatedRangeTest {

    private List<String> listSql(DateRangeCriteria range) {
        List<String> sql = new ArrayList<>();
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        JooqModuleSetReleaseQueryRepository repo = new JooqModuleSetReleaseQueryRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), null, mock(RepositoryFactory.class));
        try {
            repo.getModuleSetReleaseList(new ModuleSetReleaseListFilterCriteria(
                    new LibraryId(BigInteger.ONE), null, null, null, null, range), new PageRequest());
        } catch (RuntimeException ignored) {
            // The mocked connection returns no rows; only the statements it received matter here.
        }
        return sql;
    }

    @Test
    void bothBoundsCompareTheModuleSetReleaseTimestamp() {
        List<String> sql = listSql(new DateRangeCriteria(new Date(1_000_000L), new Date(2_000_000L)));

        assertTrue(sql.stream().anyMatch(s -> s.contains("`module_set_release`.`last_update_timestamp` >= ?")), sql.toString());
        assertTrue(sql.stream().anyMatch(s -> s.contains("`module_set_release`.`last_update_timestamp` < ?")), sql.toString());
        assertTrue(sql.stream().noneMatch(s -> s.contains("`module_set`.`last_update_timestamp`")), sql.toString());
    }
}
