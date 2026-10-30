package org.oagi.score.gateway.http.api.cc_management.service;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** The "My unused extensions in BIEs" query matches the BIE's ASCCP by manifest, as the BCC half does. */
class CcListServiceUnusedExtensionsTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));
    private final LibraryId libraryId = new LibraryId(BigInteger.ONE);

    private final List<String> sql = new ArrayList<>();
    private ReleaseQueryRepository releaseQuery;
    private CcListService service;

    @BeforeEach
    void setUp() {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        releaseQuery = mock(ReleaseQueryRepository.class);
        when(repositoryFactory.releaseQueryRepository(requester)).thenReturn(releaseQuery);

        service = new CcListService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "dslContext",
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL));
    }

    @Test
    void theAsccQueryJoinsTheBieAsccpOnTheManifestId() {
        ReleaseSummaryRecord workingRelease = mock(ReleaseSummaryRecord.class);
        when(workingRelease.releaseId()).thenReturn(new ReleaseId(BigInteger.valueOf(9)));
        when(releaseQuery.getReleaseSummary(libraryId, "Working")).thenReturn(workingRelease);

        assertTrue(service.getMyExtensionsUnusedInBIEs(requester, libraryId).isEmpty());

        List<String> asccQueries = sql.stream().filter(s -> s.contains("`ascc_manifest`")).toList();
        assertEquals(1, asccQueries.size(), sql.toString());
        String ascc = asccQueries.get(0);
        assertTrue(ascc.contains("`asbiep`.`based_asccp_manifest_id` = `bie_manifest`.`asccp_manifest_id`"), ascc);
        assertTrue(ascc.contains("`ascc_manifest`.`from_acc_manifest_id` = `acc_manifest`.`acc_manifest_id`"), ascc);
    }

    @Test
    void theBccQueryTiesTheBccManifestToTheAccManifest() {
        ReleaseSummaryRecord workingRelease = mock(ReleaseSummaryRecord.class);
        when(workingRelease.releaseId()).thenReturn(new ReleaseId(BigInteger.valueOf(9)));
        when(releaseQuery.getReleaseSummary(libraryId, "Working")).thenReturn(workingRelease);

        service.getMyExtensionsUnusedInBIEs(requester, libraryId);

        assertTrue(sql.stream().anyMatch(s -> s.contains("`bcc_manifest`.`from_acc_manifest_id` = `acc_manifest`.`acc_manifest_id`")),
                sql.toString());
    }

    @Test
    void withoutAWorkingReleaseThereIsNothingToList() {
        when(releaseQuery.getReleaseSummary(libraryId, "Working")).thenReturn(null);

        assertTrue(service.getMyExtensionsUnusedInBIEs(requester, libraryId).isEmpty());
        assertTrue(sql.isEmpty(), sql.toString());
    }
}
