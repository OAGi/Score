package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.DeleteBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.OasResourceId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasOperationRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasRequestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasResponseRecord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_OPERATION;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_REQUEST;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_RESPONSE;

/**
 * Issue #1492 (Option 2) -- CRITICAL §3.5 fix (stress-review H1). Drives
 * {@link JooqBieForOasDocCommandRepository#deleteBieForOasDoc} through a jOOQ {@link MockDataProvider}
 * and asserts the SQL that is (and is not) emitted:
 *
 *  - removing one body of a TWO-body operation leaves the sibling body + operation + resource intact
 *    (i.e. no DELETE FROM oas_operation / oas_resource is issued); and
 *  - removing the LAST body of an operation deletes the operation AND its resource.
 *
 * The old code unconditionally deleted oas_operation + oas_resource per body, which orphaned/
 * FK-violated a surviving sibling body.
 */
class BieForOasDocDeletePathTest {

    private static final long OPERATION_ID = 200L;
    private static final long RESOURCE_ID = 100L;
    private static final long REQUEST_ID = 400L;
    private static final long RESPONSE_ID = 500L;
    private static final long MESSAGE_BODY_ID = 300L;

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
            "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

    /**
     * Records every executed statement and answers the SELECTs the delete path issues.
     *
     * @param siblingResponseExists when true, a sibling oas_response still references the operation
     *                              after the request is deleted (operation must NOT be deleted).
     */
    private final class RecordingProvider implements MockDataProvider {
        final List<String> sql = new ArrayList<>();
        private final boolean siblingResponseExists;

        RecordingProvider(boolean siblingResponseExists) {
            this.siblingResponseExists = siblingResponseExists;
        }

        @Override
        public MockResult[] execute(MockExecuteContext ctx) {
            String s = ctx.sql().toLowerCase(Locale.ROOT);
            sql.add(s);
            DSLContext create = DSL.using(SQLDialect.MYSQL);

            boolean isSelect = s.startsWith("select");
            boolean isExists = s.contains("exists");

            // fetchExists(OAS_REQUEST ...) -> after deleting the request, none remains.
            if (isExists && s.contains("`oas_request`")) {
                return existsResult(false);
            }
            // fetchExists(OAS_RESPONSE ...) -> a sibling response may still exist.
            if (isExists && s.contains("`oas_response`")) {
                return existsResult(siblingResponseExists);
            }
            // fetchExists(OAS_OPERATION ...) for the remaining-operation check -> none remains.
            if (isExists && s.contains("`oas_operation`")) {
                return existsResult(false);
            }

            // SELECT the request row being deleted (selectFrom OAS_REQUEST, schema-qualified).
            if (isSelect && s.contains("`oas_request`")) {
                Result<OasRequestRecord> r = create.newResult(OAS_REQUEST);
                OasRequestRecord rec = create.newRecord(OAS_REQUEST);
                rec.setOasRequestId(org.jooq.types.ULong.valueOf(REQUEST_ID));
                rec.setOasOperationId(org.jooq.types.ULong.valueOf(OPERATION_ID));
                rec.setOasMessageBodyId(org.jooq.types.ULong.valueOf(MESSAGE_BODY_ID));
                r.add(rec);
                return new MockResult[] { new MockResult(1, r) };
            }

            // selectFrom OAS_OPERATION (the deleteOperationAndResourceIfEmpty operation lookup).
            if (isSelect && s.contains("`oas_operation`")) {
                Result<OasOperationRecord> r = create.newResult(OAS_OPERATION);
                OasOperationRecord rec = create.newRecord(OAS_OPERATION);
                rec.setOasOperationId(org.jooq.types.ULong.valueOf(OPERATION_ID));
                rec.setOasResourceId(org.jooq.types.ULong.valueOf(RESOURCE_ID));
                r.add(rec);
                return new MockResult[] { new MockResult(1, r) };
            }

            // SELECT the response row being deleted (selectFrom OAS_RESPONSE). Excludes oas_response_headers;
            // the fetchExists(OAS_RESPONSE) form is caught above by the isExists branch.
            if (isSelect && s.contains("`oas_response`") && !s.contains("oas_response_headers")) {
                Result<OasResponseRecord> r = create.newResult(OAS_RESPONSE);
                OasResponseRecord rec = create.newRecord(OAS_RESPONSE);
                rec.setOasResponseId(org.jooq.types.ULong.valueOf(RESPONSE_ID));
                rec.setOasOperationId(org.jooq.types.ULong.valueOf(OPERATION_ID));
                rec.setOasMessageBodyId(org.jooq.types.ULong.valueOf(MESSAGE_BODY_ID));
                r.add(rec);
                return new MockResult[] { new MockResult(1, r) };
            }

            // Any other SELECT (e.g. selectFrom OAS_RESOURCE_TAG) -> no rows.
            if (isSelect) {
                return new MockResult[] { new MockResult(0, create.newResult()) };
            }

            // INSERT/UPDATE/DELETE -> report 1 affected row.
            return new MockResult[] { new MockResult(1, null) };
        }

        private MockResult[] existsResult(boolean exists) {
            DSLContext create = DSL.using(SQLDialect.MYSQL);
            // jOOQ's fetchExists wraps the query in SELECT EXISTS(...), which ALWAYS returns exactly one
            // row with a single 0/1 value. Mirror that (a zero-row result makes fetchValue NPE).
            org.jooq.Field<Integer> v = DSL.field("v", Integer.class);
            Result<org.jooq.Record1<Integer>> r = create.newResult(v);
            org.jooq.Record1<Integer> rec = create.newRecord(v);
            rec.value1(exists ? 1 : 0);
            r.add(rec);
            return new MockResult[] { new MockResult(1, r) };
        }
    }

    private JooqBieForOasDocCommandRepository repo(RecordingProvider provider) {
        MockConnection connection = new MockConnection(provider);
        DSLContext dslContext = DSL.using(connection, SQLDialect.MYSQL);
        return new JooqBieForOasDocCommandRepository(dslContext, requester, null);
    }

    private DeleteBieForOasDocRequest deleteRequestForRequestBody() {
        BieForOasDoc body = new BieForOasDoc();
        body.setMessageBody("Request");
        body.setVerb("POST");
        body.setResourceName("/orders");
        body.setOasOperationId(new OasOperationId(BigInteger.valueOf(OPERATION_ID)));
        body.setOasResourceId(new OasResourceId(BigInteger.valueOf(RESOURCE_ID)));
        DeleteBieForOasDocRequest request = new DeleteBieForOasDocRequest(requester);
        request.setOasDocId(new OasDocId(BigInteger.valueOf(1)));
        request.setBieForOasDocList(List.of(body));
        return request;
    }

    private DeleteBieForOasDocRequest deleteRequestForResponseBody() {
        BieForOasDoc body = new BieForOasDoc();
        body.setMessageBody("Response");
        body.setVerb("POST");
        body.setResourceName("/orders");
        body.setOasOperationId(new OasOperationId(BigInteger.valueOf(OPERATION_ID)));
        body.setOasResourceId(new OasResourceId(BigInteger.valueOf(RESOURCE_ID)));
        DeleteBieForOasDocRequest request = new DeleteBieForOasDocRequest(requester);
        request.setOasDocId(new OasDocId(BigInteger.valueOf(1)));
        request.setBieForOasDocList(List.of(body));
        return request;
    }

    private static int indexOfFirst(List<String> sqlLog, Predicate<String> predicate) {
        for (int i = 0; i < sqlLog.size(); i++) {
            if (predicate.test(sqlLog.get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Test
    void removingOneBodyOfTwoBodyOperation_keepsSiblingAndOperationAndResource() {
        RecordingProvider provider = new RecordingProvider(/* siblingResponseExists */ true);
        repo(provider).deleteBieForOasDoc(deleteRequestForRequestBody());

        // The targeted Request body IS deleted.
        assertTrue(provider.sql.stream().anyMatch(s -> s.startsWith("delete") && s.contains("`oas_request`")),
                "the targeted oas_request must be deleted");
        // The operation and resource are NOT deleted (a sibling Response still uses the operation).
        assertFalse(provider.sql.stream().anyMatch(s -> s.startsWith("delete") && s.contains("`oas_operation`")),
                "oas_operation must NOT be deleted while a sibling body remains");
        assertFalse(provider.sql.stream().anyMatch(s -> s.startsWith("delete") && s.contains("`oas_resource`")
                        && !s.contains("oas_resource_tag")),
                "oas_resource must NOT be deleted while the operation remains");
    }

    @Test
    void removingLastBodyOfOperation_deletesOperationAndResource() {
        RecordingProvider provider = new RecordingProvider(/* siblingResponseExists */ false);
        repo(provider).deleteBieForOasDoc(deleteRequestForRequestBody());

        // The targeted Request body IS deleted.
        assertTrue(provider.sql.stream().anyMatch(s -> s.startsWith("delete") && s.contains("`oas_request`")),
                "the targeted oas_request must be deleted");
        // With no sibling body remaining, the operation AND its resource are deleted.
        assertTrue(provider.sql.stream().anyMatch(s -> s.startsWith("delete") && s.contains("`oas_operation`")),
                "oas_operation must be deleted when its last body is removed");
        assertTrue(provider.sql.stream().anyMatch(s -> s.startsWith("delete") && s.contains("`oas_resource`")
                        && !s.contains("oas_resource_tag")),
                "oas_resource must be deleted when its last operation is removed");
    }

    /**
     * Issue #1492 (mirror of the Request-side deletes): removing a Response body must delete the
     * Response's children (oas_response_headers and the oas_parameter_link RESTRICT FK keyed by
     * oas_response_id) BEFORE the oas_response row itself, or the FK delete fails.
     */
    @Test
    void removingResponseBody_deletesResponseHeadersAndParameterLink_beforeOasResponseRow() {
        RecordingProvider provider = new RecordingProvider(/* siblingResponseExists */ false);
        repo(provider).deleteBieForOasDoc(deleteRequestForResponseBody());

        // The `oas_response` row delete is unique: oas_response_headers / oas_parameter_link rows
        // never carry the backtick-delimited `oas_response` token.
        int responseRowDelete = indexOfFirst(provider.sql,
                s -> s.startsWith("delete") && s.contains("`oas_response`"));
        int headersDelete = indexOfFirst(provider.sql,
                s -> s.startsWith("delete") && s.contains("`oas_response_headers`"));
        int paramLinkByResponseDelete = indexOfFirst(provider.sql,
                s -> s.startsWith("delete") && s.contains("`oas_parameter_link`") && s.contains("`oas_response_id`"));

        assertTrue(responseRowDelete >= 0, "the targeted oas_response row must be deleted");
        assertTrue(headersDelete >= 0 && headersDelete < responseRowDelete,
                "oas_response_headers must be deleted before the oas_response row");
        assertTrue(paramLinkByResponseDelete >= 0 && paramLinkByResponseDelete < responseRowDelete,
                "oas_parameter_link (by oas_response_id) must be deleted before the oas_response row");
        // No sibling body remains -> the now-empty operation is deleted too.
        assertTrue(provider.sql.stream().anyMatch(s -> s.startsWith("delete") && s.contains("`oas_operation`")),
                "the operation is deleted once its last (Response) body is removed");
    }
}
