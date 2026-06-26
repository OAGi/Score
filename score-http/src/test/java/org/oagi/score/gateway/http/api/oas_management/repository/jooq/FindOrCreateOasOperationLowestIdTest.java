package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
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
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.InsertOasOperationArguments;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_OPERATION;

/**
 * Issue #1492 (code-level find-or-create, no DB unique constraint): when two legacy duplicate
 * operations exist for one {@code (oas_resource_id, verb)}, {@link JooqOasDocCommandRepository#findOrCreateOasOperation}
 * resolves to the LOWEST id (its lookup is {@code ORDER BY oas_operation_id ASC LIMIT 1}) and issues
 * NO insert, rather than throwing.
 */
class FindOrCreateOasOperationLowestIdTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
            "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

    private static final class RecordingProvider implements MockDataProvider {
        final List<String> sql = new ArrayList<>();

        @Override
        public MockResult[] execute(MockExecuteContext ctx) {
            String s = ctx.sql().toLowerCase(Locale.ROOT);
            sql.add(s);
            DSLContext create = DSL.using(SQLDialect.MYSQL);
            // the find-or-create lookup: SELECT oas_operation_id ... ORDER BY ... LIMIT 1 -> lowest id 50
            if (s.startsWith("select") && s.contains("`oas_operation`") && s.contains("order by")) {
                Field<ULong> idField = OAS_OPERATION.OAS_OPERATION_ID;
                Result<Record1<ULong>> r = create.newResult(idField);
                Record1<ULong> rec = create.newRecord(idField);
                rec.value1(ULong.valueOf(50L));
                r.add(rec);
                return new MockResult[] { new MockResult(1, r) };
            }
            return new MockResult[] { new MockResult(1, null) };
        }
    }

    @Test
    void findOrCreateOasOperation_existingLowestIdWins_andIssuesNoInsert() {
        RecordingProvider provider = new RecordingProvider();
        JooqOasDocCommandRepository repo = new JooqOasDocCommandRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), requester, null);

        InsertOasOperationArguments args = new InsertOasOperationArguments(repo)
                .setOasResourceId(ULong.valueOf(100L))
                .setVerb("POST")
                .setUserId(new UserId(BigInteger.valueOf(42)));

        OasOperationId result = repo.findOrCreateOasOperation(args);

        assertEquals(BigInteger.valueOf(50L), result.value(), "the existing (lowest-id) operation wins");
        assertFalse(provider.sql.stream().anyMatch(s -> s.startsWith("insert")),
                "an existing operation was found -> no INSERT may be issued");
        assertTrue(provider.sql.stream().anyMatch(s -> s.startsWith("select")
                        && s.contains("order by") && s.contains("`oas_operation_id`")
                        && s.contains("asc") && s.contains("limit")),
                "the lookup orders by oas_operation_id asc and limits to one");
    }
}
