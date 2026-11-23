package org.oagi.score.gateway.http.api.business_term_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.AssignBusinessTermRequest;
import org.oagi.score.gateway.http.api.business_term_management.model.BieToAssign;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.AsbieBbieListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * SQL-level coverage (jOOQ {@link MockDataProvider}, no database) of Issue #346: an ASBIE/BBIE that is
 * not used is not part of the BIE, so it is neither listed for a Business Term assignment nor accepted
 * by {@code assignBusinessTerm}. The lookup by id lists (rows that are already assigned) stays unfiltered.
 */
class JooqBusinessTermUsedAssociationTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "eu", "End User",
            "eu@example.com", true, List.of(ScoreRole.END_USER));

    /** Records every statement; every SELECT answers "no row" (a count query answers 0). */
    private static final class RecordingProvider implements MockDataProvider {
        final List<String> sql = new ArrayList<>();

        @Override
        public MockResult[] execute(MockExecuteContext ctx) {
            String s = ctx.sql().toLowerCase(Locale.ROOT);
            sql.add(s);
            var create = DSL.using(SQLDialect.MYSQL);
            if (s.contains("count(")) {
                var count = create.newResult(DSL.field("c", Integer.class));
                var record = create.newRecord(DSL.field("c", Integer.class));
                record.set(DSL.field("c", Integer.class), 0);
                count.add(record);
                return new MockResult[]{new MockResult(1, count)};
            }
            return new MockResult[]{new MockResult(0, create.newResult(DSL.field("c", Integer.class)))};
        }
    }

    private DSLContext dsl(RecordingProvider provider) {
        return DSL.using(new MockConnection(provider), SQLDialect.MYSQL);
    }

    private static String only(List<String> sql, String prefix) {
        return sql.stream().filter(s -> s.startsWith(prefix)).findFirst().orElseThrow();
    }

    @Test
    void filteredList_offersOnlyUsedAsbieAndBbie() {
        RecordingProvider provider = new RecordingProvider();
        var repo = new JooqBusinessTermQueryRepository(dsl(provider), requester,
                mock(RepositoryFactory.class, RETURNS_DEEP_STUBS));

        repo.getAsbieBbieList(AsbieBbieListFilterCriteria.builder().build(), new PageRequest());

        assertFalse(provider.sql.isEmpty());
        for (String s : provider.sql) {
            assertTrue(s.contains("`asbie`.`is_used` = ?"), "ASBIE branch must require is_used: " + s);
            assertTrue(s.contains("`bbie`.`is_used` = ?"), "BBIE branch must require is_used: " + s);
        }
    }

    @Test
    void filteredList_typeFilterKeepsTheUsedConditionOnTheSelectedBranch() {
        RecordingProvider provider = new RecordingProvider();
        var repo = new JooqBusinessTermQueryRepository(dsl(provider), requester,
                mock(RepositoryFactory.class, RETURNS_DEEP_STUBS));

        repo.getAsbieBbieList(AsbieBbieListFilterCriteria.builder().typeList(List.of("BBIE")).build(),
                new PageRequest());

        for (String s : provider.sql) {
            assertTrue(s.contains("`bbie`.`is_used` = ?"), s);
            assertFalse(s.contains("`asbie`.`is_used`"), "ASBIE branch is not queried: " + s);
        }
    }

    @Test
    void lookupByIdLists_isNotFilteredByIsUsed() {
        RecordingProvider provider = new RecordingProvider();
        var repo = new JooqBusinessTermQueryRepository(dsl(provider), requester,
                mock(RepositoryFactory.class, RETURNS_DEEP_STUBS));

        repo.getAsbieBbieList(List.of(new AsbieId(BigInteger.ONE)), List.of(new BbieId(BigInteger.TWO)));

        assertFalse(provider.sql.isEmpty());
        for (String s : provider.sql) {
            assertFalse(s.contains("`is_used`"), "an already assigned row must stay listed: " + s);
        }
    }

    private JooqBusinessTermCommandRepository commandRepo(RecordingProvider provider) {
        return new JooqBusinessTermCommandRepository(dsl(provider), requester, null);
    }

    private static AssignBusinessTermRequest request(String type) {
        return new AssignBusinessTermRequest(List.of(new BieToAssign(BigInteger.valueOf(77), type)), false, null);
    }

    @Test
    void assignToUnusedAsbie_isRejectedWithoutWriting() {
        RecordingProvider provider = new RecordingProvider(); // the used-only lookup finds no row

        var e = assertThrows(IllegalArgumentException.class, () ->
                commandRepo(provider).assignBusinessTerm(new BusinessTermId(BigInteger.TEN), request("ASBIE")));

        assertEquals("No used ASBIE found for id 77.", e.getMessage());
        assertTrue(only(provider.sql, "select").contains("`asbie`.`is_used` = ?"), provider.sql.toString());
        assertTrue(provider.sql.stream().noneMatch(s -> s.startsWith("insert") || s.startsWith("update")),
                "nothing may be written: " + provider.sql);
    }

    @Test
    void assignToUnusedBbie_isRejectedWithoutWriting() {
        RecordingProvider provider = new RecordingProvider();

        var e = assertThrows(IllegalArgumentException.class, () ->
                commandRepo(provider).assignBusinessTerm(new BusinessTermId(BigInteger.TEN), request("BBIE")));

        assertEquals("No used BBIE found for id 77.", e.getMessage());
        assertTrue(only(provider.sql, "select").contains("`bbie`.`is_used` = ?"), provider.sql.toString());
        assertTrue(provider.sql.stream().noneMatch(s -> s.startsWith("insert") || s.startsWith("update")),
                "nothing may be written: " + provider.sql);
    }
}
