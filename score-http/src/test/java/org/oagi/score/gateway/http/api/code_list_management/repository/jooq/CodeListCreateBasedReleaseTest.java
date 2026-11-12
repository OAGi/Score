package org.oagi.score.gateway.http.api.code_list_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A Code List is derived only from a base Code List of the same release. */
class CodeListCreateBasedReleaseTest {

    private static final CodeListManifestId BASE = new CodeListManifestId(BigInteger.ONE);
    private static final ReleaseId BASE_RELEASE = new ReleaseId(BigInteger.valueOf(5));
    private static final ReleaseId OTHER_RELEASE = new ReleaseId(BigInteger.valueOf(6));

    private final List<String> sql = new ArrayList<>();
    private JooqCodeListCommandRepository repository;

    @BeforeEach
    void setUp() {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql());
            return new MockResult[]{new MockResult(1, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        CodeListQueryRepository query = mock(CodeListQueryRepository.class);
        CodeListDetailsRecord details = mock(CodeListDetailsRecord.class);
        when(details.release()).thenReturn(new ReleaseSummaryRecord(BASE_RELEASE, null, "10.13.3", null));
        when(query.getCodeListDetails(BASE)).thenReturn(details);

        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.codeListQueryRepository(any())).thenReturn(query);
        when(repositories.agencyIdListQueryRepository(any())).thenReturn(mock(AgencyIdListQueryRepository.class));
        when(repositories.logCommandRepository(any())).thenReturn(mock(LogCommandRepository.class));
        ScoreUser requester = new ScoreUser(new UserId(BigInteger.ONE), "u", "U", null, false, List.of(ScoreRole.DEVELOPER));
        repository = new JooqCodeListCommandRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), requester, repositories);
    }

    @Test
    void baseCodeListOfAnotherReleaseIsRejectedBeforeAnythingIsWritten() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.create(OTHER_RELEASE, BASE));
        assertTrue(e.getMessage().contains("release"), e.getMessage());
        assertEquals(List.of(), sql);
    }
}
