package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * The Six Hexadecimal Identifier duplicate check must search for the requested value,
 * not the value currently stored on the Data Type.
 */
class HasDuplicateSixDigitIdTest {

    @Test
    void searchesForTheRequestedValue() {
        List<String> sql = new ArrayList<>();
        List<Object[]> bindings = new ArrayList<>();
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            bindings.add(ctx.bindings());
            return new MockResult[]{new MockResult(1, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        JooqDtQueryRepository repo = spy(new JooqDtQueryRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), null, mock(RepositoryFactory.class, org.mockito.Mockito.RETURNS_DEEP_STUBS)));

        DtManifestId dtManifestId = new DtManifestId(BigInteger.TEN);
        DtSummaryRecord dt = mock(DtSummaryRecord.class);
        ReleaseSummaryRecord release = mock(ReleaseSummaryRecord.class);
        when(release.releaseId()).thenReturn(new ReleaseId(BigInteger.ONE));
        when(dt.release()).thenReturn(release);
        when(dt.guid()).thenReturn(new Guid("a".repeat(32)));
        when(dt.sixDigitId()).thenReturn("OLD001");
        doReturn(dt).when(repo).getDtSummary(dtManifestId);

        repo.hasDuplicateSixDigitId(dtManifestId, "NEW002");

        assertTrue(sql.stream().anyMatch(s -> s.contains("`six_digit_id` = ?")), sql.toString());
        assertTrue(bindings.stream().anyMatch(b -> Arrays.asList(b).contains("NEW002")));
        assertTrue(bindings.stream().noneMatch(b -> Arrays.asList(b).contains("OLD001")));
    }
}
