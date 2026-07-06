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
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocRequest;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #1519: the BIE-root "OpenAPI Document Information" panel lists a BIE's bindings with a BIE-only
 * query (oasDocId == null). {@link JooqBieForOasDocQueryRepository#getBieForOasDoc} LEFT-joins each
 * half's own body table (oas_request / oas_response), so a body of the OPPOSITE kind would otherwise
 * produce a phantom row whose request/response — and therefore operation, resource and document — are
 * all null. Doc-centric callers filter those out implicitly through their oasDocId predicate, but the
 * BIE-only query has none, so the fix constrains each half of the UNION to rows whose OWN body matched
 * ({@code OAS_REQUEST_ID} / {@code OAS_RESPONSE_ID IS NOT NULL}).
 *
 * <p>This drives the query through a jOOQ {@link MockDataProvider} (no database) and asserts BOTH
 * phantom-row guards appear in the emitted SQL, so removing either one fails the test.</p>
 */
class BieForOasDocPhantomRowFilterTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
            "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

    /** Captures every executed statement; answers COUNT(*) with 0 and every other select with no rows. */
    private static final class RecordingProvider implements MockDataProvider {
        final List<String> sql = new ArrayList<>();

        @Override
        public MockResult[] execute(MockExecuteContext ctx) {
            String s = ctx.sql().toLowerCase(Locale.ROOT);
            sql.add(s);
            if (s.contains("count(")) {
                DSLContext create = DSL.using(SQLDialect.MYSQL);
                Field<Integer> countField = DSL.field("c", Integer.class);
                Result<Record1<Integer>> result = create.newResult(countField);
                Record1<Integer> record = create.newRecord(countField);
                record.value1(0);
                result.add(record);
                return new MockResult[]{new MockResult(1, result)};
            }
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult())};
        }
    }

    private GetBieForOasDocRequest bieOnlyRequest() {
        GetBieForOasDocRequest request = new GetBieForOasDocRequest(requester);
        // BIE-root panel: no oasDocId (document) filter, scoped to one top-level BIE; -1/-1 disables paging.
        request.setTopLevelAsbiepId(new TopLevelAsbiepId(BigInteger.valueOf(123)));
        request.setPageIndex(-1);
        request.setPageSize(-1);
        return request;
    }

    private String render() {
        RecordingProvider provider = new RecordingProvider();
        MockConnection connection = new MockConnection(provider);
        DSLContext dslContext = DSL.using(connection, SQLDialect.MYSQL);
        JooqBieForOasDocQueryRepository repository =
                new JooqBieForOasDocQueryRepository(dslContext, requester, null);

        repository.getBieForOasDoc(bieOnlyRequest());
        // Whitespace-normalize so the assertions do not hinge on jOOQ's inter-token spacing.
        return String.join("\n", provider.sql).replaceAll("\\s+", " ");
    }

    @Test
    void bieOnlyQuery_constrainsEachUnionHalfToItsOwnMatchedBody() {
        String sql = render();
        assertTrue(sql.contains("`oas_request`.`oas_request_id` is not null"),
                "the Request half must exclude phantom rows (OAS_REQUEST_ID IS NOT NULL). SQL:\n" + sql);
        assertTrue(sql.contains("`oas_response`.`oas_response_id` is not null"),
                "the Response half must exclude phantom rows (OAS_RESPONSE_ID IS NOT NULL). SQL:\n" + sql);
    }
}
