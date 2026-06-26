package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.DeleteBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.DeleteBieForOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #1492: defensive guards of {@link JooqBieForOasDocCommandRepository#deleteBieForOasDoc} —
 * a null {@code oasDocId} is rejected before anything is touched, a null/empty body list is a no-op
 * (no SQL), and a per-row null {@code oasOperationId} is skipped (no SQL for that row).
 */
class BieForOasDocDeleteGuardsTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
            "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

    private static final class RecordingProvider implements MockDataProvider {
        final List<String> sql = new ArrayList<>();

        @Override
        public MockResult[] execute(MockExecuteContext ctx) {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            return new MockResult[] { new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult()) };
        }
    }

    private JooqBieForOasDocCommandRepository repo(RecordingProvider provider) {
        DSLContext dslContext = DSL.using(new MockConnection(provider), SQLDialect.MYSQL);
        return new JooqBieForOasDocCommandRepository(dslContext, requester, null);
    }

    private BieForOasDoc requestBody() {
        BieForOasDoc body = new BieForOasDoc();
        body.setMessageBody("Request");
        body.setVerb("POST");
        body.setResourceName("/orders");
        // oasOperationId deliberately left null
        return body;
    }

    @Test
    void nullOasDocId_throwsIllegalArgument_beforeTouchingTheDatabase() {
        RecordingProvider provider = new RecordingProvider();
        DeleteBieForOasDocRequest request = new DeleteBieForOasDocRequest(requester);
        request.setOasDocId(null);
        request.setBieForOasDocList(List.of(requestBody())); // a body is present, yet the null-doc guard fires first

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repo(provider).deleteBieForOasDoc(request));
        assertTrue(ex.getMessage().contains("oasDocId"), "guard message names the offending parameter");
        assertTrue(provider.sql.isEmpty(), "no SQL is issued when oasDocId is null");
    }

    @Test
    void emptyAndNullBodyList_returnEmptyResponse_andIssueNoSql() {
        RecordingProvider emptyProvider = new RecordingProvider();
        DeleteBieForOasDocRequest emptyList = new DeleteBieForOasDocRequest(requester);
        emptyList.setOasDocId(new OasDocId(BigInteger.valueOf(1)));
        emptyList.setBieForOasDocList(Collections.emptyList());
        DeleteBieForOasDocResponse emptyResponse = repo(emptyProvider).deleteBieForOasDoc(emptyList);
        assertTrue(emptyResponse.getBieForOasDocList().isEmpty());
        assertTrue(emptyProvider.sql.isEmpty(), "empty body list -> no SQL");

        RecordingProvider nullProvider = new RecordingProvider();
        DeleteBieForOasDocRequest nullList = new DeleteBieForOasDocRequest(requester);
        nullList.setOasDocId(new OasDocId(BigInteger.valueOf(1)));
        nullList.setBieForOasDocList(null);
        DeleteBieForOasDocResponse nullResponse = repo(nullProvider).deleteBieForOasDoc(nullList);
        assertTrue(nullResponse.getBieForOasDocList().isEmpty());
        assertTrue(nullProvider.sql.isEmpty(), "null body list -> no SQL");
    }

    @Test
    void rowWithNullOperationId_isSkipped_noSelectNoDelete() {
        RecordingProvider provider = new RecordingProvider();
        DeleteBieForOasDocRequest request = new DeleteBieForOasDocRequest(requester);
        request.setOasDocId(new OasDocId(BigInteger.valueOf(1)));
        request.setBieForOasDocList(List.of(requestBody())); // operationId is null on the row

        repo(provider).deleteBieForOasDoc(request);

        assertFalse(provider.sql.stream().anyMatch(s -> s.startsWith("delete")), "no DELETE for a row with null operationId");
        assertFalse(provider.sql.stream().anyMatch(s -> s.startsWith("select")), "no SELECT for a row with null operationId");
    }
}
