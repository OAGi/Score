package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListInBiePackageFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BiePackageListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Business Context filter of the BIE Package list and of the BIE list in a BIE package must reach the query,
 * as a match on the name of the Business Context. The statements are captured, not run: mapping rows is not under test.
 */
class BiePackageBusinessContextFilterTest {

    private static final String NAME_MATCH = "lower(`biz_ctx`.`name`) like";

    /** The statements that {@code call} issues, each with its bound values appended. */
    private List<String> statements(java.util.function.Consumer<JooqBiePackageQueryRepository> call) {
        List<String> sql = new ArrayList<>();
        var repository = new JooqBiePackageQueryRepository(
                DSL.using(new MockConnection(ctx -> {
                    sql.add((ctx.sql() + " " + java.util.Arrays.toString(ctx.bindings())).toLowerCase(Locale.ROOT));
                    var create = DSL.using(SQLDialect.MYSQL);
                    var count = DSL.field("c", Integer.class);
                    var result = create.newResult(count);
                    var record = create.newRecord(count);
                    record.value1(0);
                    result.add(record);
                    return new MockResult[]{new MockResult(1, result)};
                }), SQLDialect.MYSQL), null, null);
        try {
            call.accept(repository);
        } catch (RuntimeException ignored) {
            // mapping the mocked rows is not under test; the issued statements are
        }
        return sql;
    }

    private List<String> packageList(String businessContext) {
        return statements(r -> r.getBiePackageList(new BiePackageListFilterCriteria(
                null, null, null, null, null, null, businessContext, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), null), new PageRequest()));
    }

    private List<String> bieListInPackage(String businessContext) {
        return statements(r -> r.getBieListInBiePackage(new BieListInBiePackageFilterCriteria(
                null, null, businessContext, null, null,
                List.of(), List.of(), null), new PageRequest()));
    }

    @Test
    void packageListFiltersByBusinessContextName() {
        List<String> sql = packageList("Retail");

        assertTrue(sql.stream().anyMatch(s -> s.contains(NAME_MATCH) && s.contains("[retail")), sql.toString());
    }

    @Test
    void packageListWithoutBusinessContextDoesNotFilterByIt() {
        List<String> sql = packageList(null);

        assertFalse(sql.isEmpty(), "no statement was issued");
        assertTrue(sql.stream().noneMatch(s -> s.contains(NAME_MATCH)), sql.toString());
    }

    @Test
    void bieListInPackageFiltersByBusinessContextName() {
        List<String> sql = bieListInPackage("Retail");

        assertTrue(sql.stream().anyMatch(s -> s.contains(NAME_MATCH) && s.contains("[retail")), sql.toString());
    }

    @Test
    void bieListInPackageWithoutBusinessContextDoesNotFilterByIt() {
        List<String> sql = bieListInPackage(null);

        assertFalse(sql.isEmpty(), "no statement was issued");
        assertTrue(sql.stream().noneMatch(s -> s.contains(NAME_MATCH)), sql.toString());
    }
}
