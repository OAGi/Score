package org.oagi.score.gateway.http.api.code_list_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.*;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Uplifting a code list that has a base code list reports each of its values that the base code list already
 * defines, so the Overwritten Values dialog can show them.
 */
class CodeListUpliftDuplicateValuesTest {

    private static final ReleaseId SOURCE_RELEASE = new ReleaseId(BigInteger.ONE);
    private static final ReleaseId TARGET_RELEASE = new ReleaseId(BigInteger.TWO);
    private static final ScoreUser REQUESTER =
            new ScoreUser(new UserId(BigInteger.TEN), "tester", "tester", null, false, null);

    private final AtomicLong ids = new AtomicLong(1000);
    private final CodeListQueryRepository queries = mock(CodeListQueryRepository.class);

    /** Every INSERT ... RETURNING gets the next generated key; nothing else returns rows. */
    private JooqCodeListCommandRepository repository() {
        MockDataProvider provider = ctx -> {
            var field = DSL.field("id", ULong.class);
            var result = DSL.using(SQLDialect.MYSQL).newResult(field);
            if (ctx.sql().toLowerCase().startsWith("insert")) {
                result.add(DSL.using(SQLDialect.MYSQL).newRecord(field).values(ULong.valueOf(ids.incrementAndGet())));
            }
            return new MockResult[]{new MockResult(1, result)};
        };
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.codeListQueryRepository(any())).thenReturn(queries);
        when(repositories.agencyIdListQueryRepository(any())).thenReturn(mock(AgencyIdListQueryRepository.class));
        when(repositories.logCommandRepository(any())).thenReturn(mock(LogCommandRepository.class));
        return new JooqCodeListCommandRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), REQUESTER, repositories);
    }

    private static CodeListManifestId manifest(long id) {
        return new CodeListManifestId(BigInteger.valueOf(id));
    }

    private static CodeListValueDetailsRecord value(String value) {
        return new CodeListValueDetailsRecord(null, new CodeListValueId(BigInteger.ONE), null, null,
                value, value, null, false, false, null, null, null, null, null, null);
    }

    private static CodeListDetailsRecord codeList(long manifestId, ReleaseId releaseId, CodeListSummaryRecord based,
                                                  CodeListManifestId next, String... values) {
        return new CodeListDetailsRecord(null, new ReleaseSummaryRecord(releaseId, null, null, null),
                manifest(manifestId), new CodeListId(BigInteger.valueOf(manifestId)), null, null, based, null,
                "list", "id", "1.0", null, null, null, false, false, false, null, null,
                null, null, null, null, null, null, next,
                Arrays.stream(values).map(CodeListUpliftDuplicateValuesTest::value).toList());
    }

    private static CodeListSummaryRecord summary(long manifestId) {
        return new CodeListSummaryRecord(manifest(manifestId), new CodeListId(BigInteger.valueOf(manifestId)),
                null, null, null, null, "base", null, null, null, null, false, null, null, null, null, null);
    }

    @Test
    void valuesTheBaseCodeListAlreadyDefinesAreReportedCaseInsensitively() {
        // The base code list was uplifted to the target release as manifest 20; it defines "a" and "B".
        when(queries.getCodeListDetails(manifest(20))).thenReturn(codeList(20, TARGET_RELEASE, null, null, "a", "B"));
        when(queries.getCodeListDetails(manifest(10)))
                .thenReturn(codeList(10, SOURCE_RELEASE, null, manifest(20), "a", "B"));
        when(queries.getCodeListDetails(manifest(1)))
                .thenReturn(codeList(1, SOURCE_RELEASE, summary(10), null, "A", "b", "C"));

        var result = repository().uplift(REQUESTER, manifest(1), TARGET_RELEASE);

        assertEquals(List.of("a", "b"), result.getSecond());
    }

    @Test
    void aCodeListWithoutADuplicateValueReportsNothing() {
        when(queries.getCodeListDetails(manifest(20))).thenReturn(codeList(20, TARGET_RELEASE, null, null, "a"));
        when(queries.getCodeListDetails(manifest(10)))
                .thenReturn(codeList(10, SOURCE_RELEASE, null, manifest(20), "a"));
        when(queries.getCodeListDetails(manifest(1)))
                .thenReturn(codeList(1, SOURCE_RELEASE, summary(10), null, "X", "Y"));

        assertTrue(repository().uplift(REQUESTER, manifest(1), TARGET_RELEASE).getSecond().isEmpty());
    }

    @Test
    void aCodeListWithoutABaseCodeListReportsNothing() {
        when(queries.getCodeListDetails(manifest(1)))
                .thenReturn(codeList(1, SOURCE_RELEASE, null, null, "A", "A2"));

        assertTrue(repository().uplift(REQUESTER, manifest(1), TARGET_RELEASE).getSecond().isEmpty());
    }
}
