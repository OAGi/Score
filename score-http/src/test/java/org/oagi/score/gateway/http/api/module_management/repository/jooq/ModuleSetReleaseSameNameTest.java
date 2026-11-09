package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * The same-name check of a Module Set Release compares the whole trimmed name, in the release, and leaves out
 * the Module Set Release that is being updated; it does not match a name that merely contains the name.
 */
class ModuleSetReleaseSameNameTest {

    private final List<String> sql = new ArrayList<>();
    private final List<List<Object>> bindings = new ArrayList<>();

    private JooqModuleSetReleaseQueryRepository repository(boolean found) {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            bindings.add(Arrays.asList(ctx.bindings()));
            Result<Record1<Integer>> result = DSL.using(SQLDialect.MYSQL).newResult(DSL.field("exists", Integer.class));
            result.add(DSL.using(SQLDialect.MYSQL).newRecord(DSL.field("exists", Integer.class)).values(found ? 1 : 0));
            return new MockResult[]{new MockResult(1, result)};
        };
        return new JooqModuleSetReleaseQueryRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), null, mock(RepositoryFactory.class));
    }

    private static ReleaseId release(long id) {
        return new ReleaseId(BigInteger.valueOf(id));
    }

    private static ModuleSetReleaseId moduleSetRelease(long id) {
        return new ModuleSetReleaseId(BigInteger.valueOf(id));
    }

    @Test
    void comparesTheTrimmedNameForEqualityInTheReleaseAndLeavesOutTheModuleSetRelease() {
        assertTrue(repository(true).hasSameNameModuleSetRelease(release(7), moduleSetRelease(9), "  Foo  "));

        String statement = sql.get(0);
        assertTrue(statement.contains("`module_set_release`.`release_id` = ?"), statement);
        assertTrue(statement.contains("`module_set_release`.`name` = ?"), statement);
        assertTrue(statement.contains("`module_set_release`.`module_set_release_id` <> ?"), statement);
        assertFalse(statement.contains("like"), statement);
        assertTrue(bindings.get(0).contains("Foo"), bindings.toString());
        assertFalse(bindings.get(0).contains("  Foo  "), bindings.toString());
    }

    @Test
    void withoutAModuleSetReleaseItLeavesNoneOut() {
        assertFalse(repository(false).hasSameNameModuleSetRelease(release(7), null, "Foo"));

        assertFalse(sql.get(0).contains("`module_set_release_id` <> ?"), sql.get(0));
    }
}
