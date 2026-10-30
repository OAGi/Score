package org.oagi.score.gateway.http.api.business_term_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermUpsertResult;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BusinessTermRecord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BUSINESS_TERM;

/**
 * SQL-level coverage of {@link JooqBusinessTermCommandRepository#upsertByBusinessTermAndUri} through a
 * jOOQ {@link MockDataProvider} (the service tests mock the repository, so they never exercise this
 * SQL). Asserts the created/updated classification the batch importer relies on, and that the existence
 * probe + target-id lookup are collapsed into a SINGLE {@code SELECT} (no separate {@code selectCount}).
 */
class JooqBusinessTermUpsertTest {

    private static final long EXISTING_ID = 4242L;
    private static final long NEW_ID = 9001L;

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "eu", "End User",
            "eu@example.com", true, List.of(ScoreRole.END_USER));

    private final class RecordingProvider implements MockDataProvider {
        final List<String> sql = new ArrayList<>();
        private final boolean rowExists;

        RecordingProvider(boolean rowExists) {
            this.rowExists = rowExists;
        }

        @Override
        public MockResult[] execute(MockExecuteContext ctx) {
            String s = ctx.sql().toLowerCase(Locale.ROOT);
            sql.add(s);
            if (s.startsWith("select")) {
                return rowExists ? oneRow(EXISTING_ID) : empty();
            }
            if (s.startsWith("insert")) {
                return oneRow(NEW_ID); // INSERT ... RETURNING business_term_id
            }
            return new MockResult[] { new MockResult(1, null) }; // UPDATE -> 1 affected row
        }

        private MockResult[] empty() {
            return new MockResult[] { new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult(BUSINESS_TERM)) };
        }

        private MockResult[] oneRow(long id) {
            DSLContext create = DSL.using(SQLDialect.MYSQL);
            Result<BusinessTermRecord> r = create.newResult(BUSINESS_TERM);
            BusinessTermRecord rec = create.newRecord(BUSINESS_TERM);
            rec.setBusinessTermId(ULong.valueOf(id));
            r.add(rec);
            return new MockResult[] { new MockResult(1, r) };
        }
    }

    private JooqBusinessTermCommandRepository repo(RecordingProvider provider) {
        MockConnection connection = new MockConnection(provider);
        DSLContext dslContext = DSL.using(connection, SQLDialect.MYSQL);
        return new JooqBusinessTermCommandRepository(dslContext, requester, null);
    }

    private static long countSelects(List<String> sql) {
        return sql.stream().filter(s -> s.startsWith("select")).count();
    }

    @Test
    void noExistingRow_insertsAndReportsCreated() {
        RecordingProvider provider = new RecordingProvider(/* rowExists */ false);

        BusinessTermUpsertResult result = repo(provider)
                .upsertByBusinessTermAndUri("Ship To", "id-1", "http://ref/1", "def", "comment");

        assertTrue(result.created(), "no existing row -> created");
        assertEquals(BigInteger.valueOf(NEW_ID), result.businessTermId().value());
        assertEquals(1, countSelects(provider.sql), "existence probe must be a single SELECT");
        assertTrue(provider.sql.stream().anyMatch(s -> s.startsWith("insert")), "must INSERT a new row");
        assertFalse(provider.sql.stream().anyMatch(s -> s.startsWith("update")), "must not UPDATE on create");
    }

    @Test
    void existingRow_updatesLowestIdAndReportsUpdated() {
        RecordingProvider provider = new RecordingProvider(/* rowExists */ true);

        BusinessTermUpsertResult result = repo(provider)
                .upsertByBusinessTermAndUri("Ship To", "id-1", "http://ref/1", "def", "comment");

        assertFalse(result.created(), "existing row -> updated");
        assertEquals(BigInteger.valueOf(EXISTING_ID), result.businessTermId().value(),
                "the upsert updates (and returns) the resolved lowest-id row");
        assertEquals(1, countSelects(provider.sql),
                "existence + target-id must be a single SELECT (no separate selectCount)");
        // The upsert key is the (name + URI) pair: a row sharing only the URI must not be matched.
        String probe = provider.sql.stream().filter(q -> q.startsWith("select")).findFirst().orElseThrow();
        String where = probe.substring(probe.indexOf("where"));
        assertTrue(where.contains("`business_term`.`business_term` = ?")
                        && where.contains("`business_term`.`external_ref_uri` = ?"),
                "probe must match on both the business term and the external reference URI: " + where);
        assertFalse(provider.sql.stream().anyMatch(s -> s.startsWith("insert")), "must not INSERT on update");
        assertTrue(provider.sql.stream().anyMatch(s -> s.startsWith("update")), "must UPDATE the existing row");
    }

    @Test
    void insert_alwaysInsertsWithoutProbingTheUri() {
        // Issue #10: the interactive create must never look up (and so never update) a row by URI.
        RecordingProvider provider = new RecordingProvider(/* rowExists */ true);

        var id = repo(provider).insert("Client", "id-1", "http://ref/1", "def", "comment");

        assertEquals(BigInteger.valueOf(NEW_ID), id.value());
        assertEquals(0, countSelects(provider.sql), "insert must not SELECT by URI");
        assertTrue(provider.sql.stream().anyMatch(s -> s.startsWith("insert")), "must INSERT a new row");
        assertFalse(provider.sql.stream().anyMatch(s -> s.startsWith("update")), "must never UPDATE");
    }
}
