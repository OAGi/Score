package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards {@code selectDistinctReleases()} — the single-query source for the Error Response "apply to all"
 * ConfirmMessage Branch selector, which replaced fetching the whole paginated BIE list and combining
 * releases in the browser.
 *
 * <p>Renders the query SQL (no DB) and asserts the properties the correctness depends on:
 * <ul>
 *   <li>UNION (not UNION ALL) so a release shared by a Request and a Response body appears once;</li>
 *   <li>only INNER joins — TOP_LEVEL_ASBIEP + RELEASE are inner-joined so bodyless operations (no BIE,
 *       no release) are excluded, matching the former client-side {@code uniqueReleasesOf};</li>
 *   <li>oas_resource is joined on its FK ({@code oas_operation.oas_resource_id}), never PK=PK;</li>
 *   <li>both halves are scoped to the document via {@code oas_resource.oas_doc_id}.</li>
 * </ul>
 *
 * Backticks are stripped, the {@code oagi.} schema prefix removed (these tables are not aliased, so jOOQ
 * schema-qualifies them), and the string lower-cased so the assertions are not brittle to jOOQ rendering.
 */
class SelectDistinctReleasesSqlTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
            "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

    private JooqBieForOasDocQueryRepository repo() {
        DSLContext dslContext = DSL.using(SQLDialect.MYSQL);
        return new JooqBieForOasDocQueryRepository(dslContext, requester, null);
    }

    private static String normalize(String sql) {
        return sql.toLowerCase(Locale.ROOT).replace("`", "").replace("oagi.", "");
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) {
            count++;
        }
        return count;
    }

    @Test
    void selectDistinctReleases_deduplicatesAcrossRequestAndResponse_withUnionNotUnionAll() {
        String sql = normalize(repo().selectDistinctReleases(ULong.valueOf(1)).getSQL());

        assertTrue(sql.contains("union"), "distinct releases must UNION the Request and Response halves");
        assertFalse(sql.contains("union all"),
                "must use UNION (set) — UNION ALL would return a release twice when shared by both bodies");
    }

    @Test
    void selectDistinctReleases_excludesBodylessOperations_viaInnerJoinsOnly() {
        String sql = normalize(repo().selectDistinctReleases(ULong.valueOf(1)).getSQL());

        // A LEFT/OUTER join to top_level_asbiep would keep bodyless operations (null release) and leak a
        // phantom release. Every join must be inner.
        assertFalse(sql.contains("outer join"),
                "bodyless operations must be excluded — the query must use inner joins only (no outer join)");
        assertTrue(sql.contains("join top_level_asbiep"), "must join top_level_asbiep for the release id");
        assertTrue(sql.contains("join release"), "must join release for the release num");
    }

    @Test
    void selectDistinctReleases_joinsOasResourceOnForeignKey_andScopesByDocument() {
        String sql = normalize(repo().selectDistinctReleases(ULong.valueOf(1)).getSQL());

        assertTrue(sql.contains("oas_operation.oas_resource_id = oas_resource.oas_resource_id"),
                "must join oas_resource on the FK (oas_operation.oas_resource_id), not PK=PK");
        assertFalse(sql.contains("oas_operation.oas_operation_id = oas_resource.oas_resource_id"),
                "must NOT regress to a PK=PK join");
        // BOTH UNION halves must carry the doc-scope predicate. A one-sided assertTrue(contains(...)) would
        // stay green if a copy-paste drift dropped the WHERE from one half, leaking foreign-document
        // releases; count occurrences so the predicate must appear once per half.
        assertEquals(2, countOccurrences(sql, "oas_resource.oas_doc_id"),
                "both UNION halves (Request + Response) must be scoped to the document");
    }
}
