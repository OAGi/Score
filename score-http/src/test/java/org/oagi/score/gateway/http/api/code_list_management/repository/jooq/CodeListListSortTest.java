package org.oagi.score.gateway.http.api.code_list_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.criteria.CodeListListFilterCriteria;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.Sort;
import org.oagi.score.gateway.http.common.model.SortDirection;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Each column of the Code List list that can be sorted in the screen sorts the query by what the column shows:
 * a sort that the query builder does not know is dropped without a word, so every column is asked for, in both directions.
 * The query runs against a mock connection: what is checked is the ORDER BY of the SQL that it sends.
 */
class CodeListListSortTest {

    /** The id of each sortable column of the table of the screen (code-list.table.ts), and what it sorts the query by. */
    private static final List<String[]> COLUMNS = List.of(
            new String[]{"state", "`code_list`.`state`"},
            new String[]{"codeListName", "`code_list`.`name`"},
            new String[]{"basedCodeListName", "`based_code_list`.`name`"},
            new String[]{"agencyId", "`agency_id_list_value`.`value`"},
            new String[]{"versionId", "`code_list`.`version_id`"},
            new String[]{"extensible", "`code_list`.`extensible_indicator`"},
            new String[]{"revision", "`log`.`revision_num`"},
            new String[]{"owner", "`owner`.`login_id`"},
            new String[]{"module", "`module`.`path`"},
            new String[]{"lastUpdateTimestamp", "`code_list`.`last_update_timestamp`"});

    private final List<String> sql = new ArrayList<>();

    static Stream<Arguments> everyColumnInBothDirections() {
        return COLUMNS.stream().flatMap(column -> Stream.of(SortDirection.values())
                .map(direction -> Arguments.of(column[0], column[1], direction)));
    }

    @ParameterizedTest(name = "{0} {2}")
    @MethodSource("everyColumnInBothDirections")
    void sortsByTheColumn(String column, String expected, SortDirection direction) {
        list(new Sort(column, direction));

        assertEquals(expected + (direction == SortDirection.DESC ? " desc" : " asc"), orderBy(), sql.toString());
    }

    @Test
    void sortsByTheColumnsInTheOrderAsked() {
        list(new Sort("state", SortDirection.ASC), new Sort("codeListName", SortDirection.DESC));

        assertEquals("`code_list`.`state` asc, `code_list`.`name` desc", orderBy(), sql.toString());
    }

    @Test
    void doesNotSortByAColumnThatIsNotKnown() {
        list(new Sort("unknown", SortDirection.ASC));

        assertFalse(listSql().contains("order by"), sql.toString());
    }

    @Test
    void doesNotSortWithoutAColumn() {
        list();

        assertFalse(listSql().contains("order by"), sql.toString());
    }

    private void list(Sort... sorts) {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            var dsl = DSL.using(SQLDialect.MYSQL);
            var result = dsl.newResult(DSL.field("c", Integer.class));
            if (ctx.sql().toLowerCase(Locale.ROOT).contains("count(")) {
                result.add(dsl.newRecord(DSL.field("c", Integer.class)).values(0));
            }
            return new MockResult[]{new MockResult(result.size(), result)};
        };
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.agencyIdListQueryRepository(any())).thenReturn(mock(AgencyIdListQueryRepository.class));
        new JooqCodeListQueryRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), null, repositories)
                .getCodeListList(
                        new CodeListListFilterCriteria(
                                new LibraryId(BigInteger.ONE), new ReleaseId(BigInteger.ONE),
                                null, null, null, null, null, null, null, null,
                                null, null, null, null, null),
                        new PageRequest(0, 10, List.of(sorts)));
    }

    /** The query that lists the code lists: the one that is not the count. */
    private String listSql() {
        return sql.stream().filter(e -> !e.contains("count(")).findFirst().orElseThrow();
    }

    /** What the list query is ordered by, up to its LIMIT. */
    private String orderBy() {
        String listSql = listSql();
        int at = listSql.indexOf(" order by ");
        assertTrue(at > -1, "no ORDER BY: " + sql);
        String tail = listSql.substring(at + " order by ".length());
        int limit = tail.indexOf(" limit ");
        return limit > -1 ? tail.substring(0, limit) : tail;
    }
}
