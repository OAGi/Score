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
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateBieForOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.OasResourceId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasOperationRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasRequestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasResourceRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasResourceTagRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasTagRecord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_OPERATION;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_REQUEST;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_RESOURCE;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_RESOURCE_TAG;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.OAS_TAG;

/**
 * Locks in the change-detection contract of
 * {@link JooqBieForOasDocCommandRepository#updateBieForOasDoc} (the part not exercised by
 * {@link BieForOasDocFlipConversionTest}, which only covers the flip insert/delete). Drives the update
 * through a jOOQ {@link MockDataProvider} and asserts which UPDATE statements are emitted and the
 * response's {@code changed} flag, so the extracted per-entity helpers keep the prior behavior:
 * <ul>
 *   <li>a tag RENAME emits an {@code update oas_tag} and flags {@code changed};</li>
 *   <li>an operationId change emits an {@code update oas_operation};</li>
 *   <li>a present operation is always audit-touched, so a no-user-change edit still reports
 *       {@code changed = true}.</li>
 * </ul>
 */
class BieForOasDocUpdateChangeFlagTest {

    private static final long DOC_ID = 1L;
    private static final long OPERATION_ID = 200L;
    private static final long RESOURCE_ID = 100L;
    private static final long REQUEST_ID = 400L;
    private static final long TAG_ID = 600L;
    private static final String STORED_TAG_NAME = "old-tag";
    private static final String STORED_OPERATION_ID = "createOrder";

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
            "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

    private final class RecordingProvider implements MockDataProvider {
        final List<String> sql = new ArrayList<>();
        private final boolean tagLinkExists;

        RecordingProvider(boolean tagLinkExists) {
            this.tagLinkExists = tagLinkExists;
        }

        @Override
        public MockResult[] execute(MockExecuteContext ctx) {
            String s = ctx.sql().toLowerCase(Locale.ROOT);
            sql.add(s);
            boolean isSelect = s.startsWith("select");
            // Backtick-delimited tokens disambiguate the tables whose names are prefixes of others.
            if (isSelect && s.contains("`oas_resource_tag`")) {
                return tagLinkExists ? oneResourceTag() : empty();
            }
            if (isSelect && s.contains("`oas_tag`")) {
                return oneTag();
            }
            if (isSelect && s.contains("`oas_resource`")) {
                return oneResource();
            }
            if (isSelect && s.contains("`oas_operation`")) {
                return oneOperation();
            }
            if (isSelect && s.contains("`oas_request`")) {
                return oneRequest();
            }
            if (isSelect) {
                return empty();
            }
            return new MockResult[] { new MockResult(1, null) };
        }

        private MockResult[] empty() {
            return new MockResult[] { new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult()) };
        }

        @SuppressWarnings("unused")
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
            rec.setOperationId(STORED_OPERATION_ID);
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
            rec.setMakeArrayIndicator((byte) 0);
            rec.setSuppressRootIndicator((byte) 0);
            r.add(rec);
            return new MockResult[] { new MockResult(1, r) };
        }

        private MockResult[] oneResourceTag() {
            DSLContext create = DSL.using(SQLDialect.MYSQL);
            Result<OasResourceTagRecord> r = create.newResult(OAS_RESOURCE_TAG);
            OasResourceTagRecord rec = create.newRecord(OAS_RESOURCE_TAG);
            rec.setOasTagId(ULong.valueOf(TAG_ID));
            rec.setOasOperationId(ULong.valueOf(OPERATION_ID));
            r.add(rec);
            return new MockResult[] { new MockResult(1, r) };
        }

        private MockResult[] oneTag() {
            DSLContext create = DSL.using(SQLDialect.MYSQL);
            Result<OasTagRecord> r = create.newResult(OAS_TAG);
            OasTagRecord rec = create.newRecord(OAS_TAG);
            rec.setOasTagId(ULong.valueOf(TAG_ID));
            rec.setName(STORED_TAG_NAME);
            r.add(rec);
            return new MockResult[] { new MockResult(1, r) };
        }
    }

    private JooqBieForOasDocCommandRepository repo(RecordingProvider provider) {
        MockConnection connection = new MockConnection(provider);
        DSLContext dslContext = DSL.using(connection, SQLDialect.MYSQL);
        return new JooqBieForOasDocCommandRepository(dslContext, requester, null);
    }

    private BieForOasDoc requestRow(String tagName, String operationId) {
        BieForOasDoc row = new BieForOasDoc();
        row.setMessageBody("Request");
        row.setVerb("POST");
        row.setResourceName("/orders");
        row.setOperationId(operationId);
        row.setTagName(tagName);
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

    private static boolean hasUpdate(List<String> sql, String table) {
        return sql.stream().anyMatch(s -> s.startsWith("update") && s.contains("`" + table + "`"));
    }

    @Test
    void renameTagAndOperationId_emitsTagAndOperationUpdates_andFlagsChanged() {
        RecordingProvider provider = new RecordingProvider(/* tagLinkExists */ true);
        UpdateBieForOasDocResponse response =
                repo(provider).updateBieForOasDoc(updateRequest(requestRow("new-tag", "renamedOp")));

        assertTrue(hasUpdate(provider.sql, "oas_tag"), "a tag rename must emit an oas_tag UPDATE");
        assertTrue(hasUpdate(provider.sql, "oas_operation"), "an operationId change must emit an oas_operation UPDATE");
        // The path is unchanged (submitted "/orders" == stored "/orders"), so no resource UPDATE fires.
        assertFalse(hasUpdate(provider.sql, "oas_resource"), "an unchanged path must NOT emit an oas_resource UPDATE");
        assertTrue(response.isChanged(), "a tag/operationId/body edit must report changed");
    }

    @Test
    void noUserVisibleChange_stillReportsChanged_becauseOperationIsAuditTouched() {
        // No tag link, path/operationId/verb all unchanged, request body indicators unchanged.
        RecordingProvider provider = new RecordingProvider(/* tagLinkExists */ false);
        UpdateBieForOasDocResponse response =
                repo(provider).updateBieForOasDoc(updateRequest(requestRow(null, STORED_OPERATION_ID)));

        assertFalse(hasUpdate(provider.sql, "oas_tag"), "no tag present -> no oas_tag UPDATE");
        // A present operation is always audit-touched, so the response reports changed even when no
        // user-visible field changed (parity with the prior inline behavior).
        assertTrue(response.isChanged(), "a present operation is always audit-touched -> changed");
    }
}
