package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #1610: guards the {@code selectForRequest()} / {@code selectForResponse()} join that pairs an
 * operation with its resource. The fix joins on the FOREIGN KEY
 * ({@code oas_operation.oas_resource_id = oas_resource.oas_resource_id}); a regression to a PK=PK join
 * ({@code oas_operation.oas_operation_id = oas_resource.oas_resource_id}) silently pairs a Request
 * operation with ANOTHER document's resource once the two auto-increment sequences diverge.
 *
 * <p>Renders the query SQL (no DB) and asserts the join condition. Backticks are stripped and the
 * string lower-cased so the assertion is not brittle to jOOQ's identifier quoting.
 */
class SelectForRequestJoinSqlTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
            "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

    private JooqBieForOasDocQueryRepository repo() {
        DSLContext dslContext = DSL.using(SQLDialect.MYSQL);
        return new JooqBieForOasDocQueryRepository(dslContext, requester, null);
    }

    private static String normalize(String sql) {
        return sql.toLowerCase(Locale.ROOT).replace("`", "");
    }

    @Test
    void selectForRequest_joinsOasResourceOnForeignKeyColumn_notPkToPk() {
        String sql = normalize(repo().selectForRequest().getSQL());

        assertTrue(sql.contains("oas_operation.oas_resource_id = oas_resource.oas_resource_id"),
                "request select must join oas_resource on the FK (oas_operation.oas_resource_id)");
        assertFalse(sql.contains("oas_operation.oas_operation_id = oas_resource.oas_resource_id"),
                "request select must NOT regress to a PK=PK join (oas_operation.oas_operation_id = oas_resource.oas_resource_id)");
    }

    @Test
    void selectForResponse_joinsOasResourceOnForeignKeyColumn_notPkToPk() {
        String sql = normalize(repo().selectForResponse().getSQL());

        assertTrue(sql.contains("oas_operation.oas_resource_id = oas_resource.oas_resource_id"),
                "response select must join oas_resource on the FK (oas_operation.oas_resource_id)");
        assertFalse(sql.contains("oas_operation.oas_operation_id = oas_resource.oas_resource_id"),
                "response select must NOT regress to a PK=PK join");
    }
}
