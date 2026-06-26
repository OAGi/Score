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
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.OasResourceId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasOperationRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasRequestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasResourceRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasResponseRecord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_MESSAGE_BODY;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_OPERATION;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_REQUEST;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_RESOURCE;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_RESPONSE;

/**
 * Message Body flip (Request &lt;-&gt; Response) on an existing operation row. Drives
 * {@link JooqBieForOasDocCommandRepository#updateBieForOasDoc} through a jOOQ {@link MockDataProvider} and
 * asserts the SQL that is (and is not) emitted.
 *
 * <p>Background: per Issue #1492 one operation owns at most one Request AND one Response (it can surface as
 * two rows). The inline "Message Body" dropdown flips an existing row's body type in place. The flip arrives
 * as the SAME {@code oas_operation_id} with the new body type and is detected by {@code getChanged()} on the
 * frontend. The previous behaviour INSERTed the new body but left the old one, so the operation ended up
 * owning BOTH bodies and re-fetched as a duplicate row. The fix converts in place: an UPDATE that INSERTs a
 * new body (only reachable via a flip, since genuine adds persist through the assign/add-operation endpoints
 * and reload) removes the now-replaced opposite body — UNLESS the same payload still carries a row of that
 * opposite type for the operation (a legitimately dual-body endpoint the user kept).
 */
class BieForOasDocFlipConversionTest {

    private static final long DOC_ID = 1L;
    private static final long OPERATION_ID = 200L;
    private static final long RESOURCE_ID = 100L;
    private static final long REQUEST_ID = 400L;
    private static final long RESPONSE_ID = 500L;
    private static final long MESSAGE_BODY_ID = 300L;

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
            "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

    /**
     * Answers the SELECTs {@code updateBieForOasDoc} issues and records every executed statement.
     *
     * @param existingRequest  whether the operation currently has an {@code oas_request} body in the DB.
     * @param existingResponse whether the operation currently has an {@code oas_response} body in the DB.
     */
    private final class RecordingProvider implements MockDataProvider {
        final List<String> sql = new ArrayList<>();
        private final boolean existingRequest;
        private final boolean existingResponse;

        RecordingProvider(boolean existingRequest, boolean existingResponse) {
            this.existingRequest = existingRequest;
            this.existingResponse = existingResponse;
        }

        @Override
        public MockResult[] execute(MockExecuteContext ctx) {
            String s = ctx.sql().toLowerCase(Locale.ROOT);
            sql.add(s);
            boolean isSelect = s.startsWith("select");
            boolean isInsert = s.startsWith("insert");

            // INSERTs whose generated id is read back via returningResult(...).
            if (isInsert && s.contains("`oas_message_body`")) {
                return returningId(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID, MESSAGE_BODY_ID);
            }
            if (isInsert && s.contains("`oas_response`")) {
                return returningId(OAS_RESPONSE.OAS_RESPONSE_ID, RESPONSE_ID);
            }
            if (isInsert && s.contains("`oas_request`")) {
                return returningId(OAS_REQUEST.OAS_REQUEST_ID, REQUEST_ID);
            }

            // SELECTs. Note: backtick-delimited tokens disambiguate `oas_resource` from `oas_resource_tag`
            // and `oas_operation` from `oas_operation_security` (those fall through to the empty default).
            if (isSelect && s.contains("`oas_resource`")) {
                return oneResource();
            }
            if (isSelect && s.contains("`oas_operation`")) {
                return oneOperation();
            }
            if (isSelect && s.contains("`oas_request`")) {
                return existingRequest ? oneRequest() : empty();
            }
            if (isSelect && s.contains("`oas_response`")) {
                return existingResponse ? oneResponse() : empty();
            }
            if (isSelect) {
                return empty();
            }

            // INSERT / UPDATE / DELETE without a read-back -> report 1 affected row.
            return new MockResult[] { new MockResult(1, null) };
        }

        private MockResult[] empty() {
            return new MockResult[] { new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult()) };
        }

        private MockResult[] returningId(Field<ULong> idField, long id) {
            DSLContext create = DSL.using(SQLDialect.MYSQL);
            Result<Record1<ULong>> r = create.newResult(idField);
            Record1<ULong> rec = create.newRecord(idField);
            rec.value1(ULong.valueOf(id));
            r.add(rec);
            return new MockResult[] { new MockResult(1, r) };
        }

        private MockResult[] oneResource() {
            DSLContext create = DSL.using(SQLDialect.MYSQL);
            Result<OasResourceRecord> r = create.newResult(OAS_RESOURCE);
            OasResourceRecord rec = create.newRecord(OAS_RESOURCE);
            rec.setOasResourceId(ULong.valueOf(RESOURCE_ID));
            rec.setOasDocId(ULong.valueOf(DOC_ID));
            rec.setPath("/orders");
            r.add(rec);
            return new MockResult[] { new MockResult(1, r) };
        }

        private MockResult[] oneOperation() {
            DSLContext create = DSL.using(SQLDialect.MYSQL);
            Result<OasOperationRecord> r = create.newResult(OAS_OPERATION);
            OasOperationRecord rec = create.newRecord(OAS_OPERATION);
            rec.setOasOperationId(ULong.valueOf(OPERATION_ID));
            rec.setOasResourceId(ULong.valueOf(RESOURCE_ID));
            rec.setVerb("POST");
            rec.setOperationId("createOrder");
            rec.setSecurityOverridden((byte) 0);
            rec.setErrorResponseBodyType("NONE");
            r.add(rec);
            return new MockResult[] { new MockResult(1, r) };
        }

        private MockResult[] oneRequest() {
            DSLContext create = DSL.using(SQLDialect.MYSQL);
            Result<OasRequestRecord> r = create.newResult(OAS_REQUEST);
            OasRequestRecord rec = create.newRecord(OAS_REQUEST);
            rec.setOasRequestId(ULong.valueOf(REQUEST_ID));
            rec.setOasOperationId(ULong.valueOf(OPERATION_ID));
            rec.setOasMessageBodyId(ULong.valueOf(MESSAGE_BODY_ID));
            rec.setMakeArrayIndicator((byte) 0);
            rec.setSuppressRootIndicator((byte) 0);
            r.add(rec);
            return new MockResult[] { new MockResult(1, r) };
        }

        private MockResult[] oneResponse() {
            DSLContext create = DSL.using(SQLDialect.MYSQL);
            Result<OasResponseRecord> r = create.newResult(OAS_RESPONSE);
            OasResponseRecord rec = create.newRecord(OAS_RESPONSE);
            rec.setOasResponseId(ULong.valueOf(RESPONSE_ID));
            rec.setOasOperationId(ULong.valueOf(OPERATION_ID));
            rec.setOasMessageBodyId(ULong.valueOf(MESSAGE_BODY_ID));
            rec.setMakeArrayIndicator((byte) 0);
            rec.setSuppressRootIndicator((byte) 0);
            r.add(rec);
            return new MockResult[] { new MockResult(1, r) };
        }
    }

    private JooqBieForOasDocCommandRepository repo(RecordingProvider provider) {
        MockConnection connection = new MockConnection(provider);
        DSLContext dslContext = DSL.using(connection, SQLDialect.MYSQL);
        return new JooqBieForOasDocCommandRepository(dslContext, requester, null);
    }

    private BieForOasDoc row(String messageBody) {
        BieForOasDoc row = new BieForOasDoc();
        row.setMessageBody(messageBody);
        row.setVerb("POST");
        row.setResourceName("/orders");
        row.setOperationId("createOrder");
        row.setOasOperationId(new OasOperationId(BigInteger.valueOf(OPERATION_ID)));
        row.setOasResourceId(new OasResourceId(BigInteger.valueOf(RESOURCE_ID)));
        row.setErrorResponseBodyType("NONE");
        return row;
    }

    private UpdateBieForOasDocRequest updateRequest(BieForOasDoc... rows) {
        UpdateBieForOasDocRequest request = new UpdateBieForOasDocRequest(requester);
        request.setOasDocId(new OasDocId(BigInteger.valueOf(DOC_ID)));
        request.setBieForOasDocList(List.of(rows));
        return request;
    }

    private static boolean hasDelete(List<String> sql, String table) {
        return sql.stream().anyMatch(s -> s.startsWith("delete") && s.contains("`" + table + "`"));
    }

    private static boolean hasInsert(List<String> sql, String table) {
        return sql.stream().anyMatch(s -> s.startsWith("insert") && s.contains("`" + table + "`"));
    }

    @Test
    void flipRequestToResponse_insertsResponse_andDeletesTheNowReplacedRequest() {
        // Operation currently owns ONLY a Request; the user flipped that row to Response.
        RecordingProvider provider = new RecordingProvider(/* existingRequest */ true, /* existingResponse */ false);
        repo(provider).updateBieForOasDoc(updateRequest(row("Response")));

        assertTrue(hasInsert(provider.sql, "oas_response"),
                "the flipped row's new Response body must be INSERTed");
        assertTrue(hasDelete(provider.sql, "oas_request"),
                "the now-replaced Request body must be deleted (convert in place, no duplicate)");
        assertFalse(hasDelete(provider.sql, "oas_response"),
                "the just-inserted Response must NOT be deleted");
    }

    @Test
    void flipResponseToRequest_insertsRequest_andDeletesTheNowReplacedResponse() {
        // Symmetric: operation currently owns ONLY a Response; the user flipped that row to Request.
        RecordingProvider provider = new RecordingProvider(/* existingRequest */ false, /* existingResponse */ true);
        repo(provider).updateBieForOasDoc(updateRequest(row("Request")));

        assertTrue(hasInsert(provider.sql, "oas_request"),
                "the flipped row's new Request body must be INSERTed");
        assertTrue(hasDelete(provider.sql, "oas_response"),
                "the now-replaced Response body must be deleted (convert in place, no duplicate)");
        assertFalse(hasDelete(provider.sql, "oas_request"),
                "the just-inserted Request must NOT be deleted");
    }

    @Test
    void dualBodyOperation_editingBothRows_deletesNeitherBody() {
        // A legitimate #1492 dual-body endpoint: the operation owns BOTH a Request and a Response, and the
        // payload carries both rows (e.g. an array-indicator edit). Neither body is INSERTed (both already
        // exist -> UPDATE path), so the convert-in-place delete never fires.
        RecordingProvider provider = new RecordingProvider(/* existingRequest */ true, /* existingResponse */ true);
        repo(provider).updateBieForOasDoc(updateRequest(row("Request"), row("Response")));

        assertFalse(hasDelete(provider.sql, "oas_request"),
                "an untouched dual-body Request must never be deleted on a plain edit");
        assertFalse(hasDelete(provider.sql, "oas_response"),
                "an untouched dual-body Response must never be deleted on a plain edit");
    }
}
