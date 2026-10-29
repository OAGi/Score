package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * The caller passes the parent module, so the same-named module is looked up among the children of that parent.
 */
class GetDuplicateModuleTest {

    @Test
    void looksUpTheSameNamedModuleAmongTheChildrenOfTheParent() {
        List<String> sql = new ArrayList<>();
        List<Object[]> bindings = new ArrayList<>();
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            bindings.add(ctx.bindings());
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        JooqModuleQueryRepository repo = new JooqModuleQueryRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), null, mock(RepositoryFactory.class));

        repo.getDuplicateModule(new ModuleId(BigInteger.valueOf(10)), "dup.xsd");

        assertTrue(sql.stream().anyMatch(s -> s.contains("`module`.`parent_module_id` = ?")), sql.toString());
        assertTrue(sql.stream().noneMatch(s -> s.contains("`module`.`module_id` = ?")), sql.toString());
        assertTrue(bindings.stream().anyMatch(b -> Arrays.asList(b).contains("dup.xsd")));
    }
}
