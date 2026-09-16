package org.oagi.score.gateway.http.api.code_list_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CodeListQueryServiceTest {

    @Test
    void filtersPreloadedCodeListsWithoutTouchingTheDatabase() {
        CodeListQueryService service = new CodeListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        ScoreUser requester = developer();
        DtManifestId dtManifestId = new DtManifestId(BigInteger.ONE);
        CodeListManifestId allowedId = new CodeListManifestId(BigInteger.TEN);
        CodeListSummaryRecord allowed = mock(CodeListSummaryRecord.class);
        CodeListSummaryRecord disallowed = mock(CodeListSummaryRecord.class);
        when(allowed.codeListManifestId()).thenReturn(allowedId);
        when(disallowed.codeListManifestId()).thenReturn(new CodeListManifestId(BigInteger.valueOf(11)));
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableCodeListListByDtManifestId(
                requester, dtManifestId, List.of(allowed, disallowed), Set.of(allowedId)))
                .containsExactly(allowed);
        verifyNoInteractions(repositoryFactory);
    }

    @Test
    void fallsBackToAllCodeListsInTheReleaseWhenTheDtHasNoExplicitCodeList() {
        CodeListQueryService service = new CodeListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        CodeListQueryRepository codeListQuery = mock(CodeListQueryRepository.class);
        DtQueryRepository dtQuery = mock(DtQueryRepository.class);
        ScoreUser requester = developer();
        DtManifestId dtManifestId = new DtManifestId(BigInteger.ONE);
        ReleaseId releaseId = new ReleaseId(BigInteger.TEN);
        ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                releaseId, null, "10.9", ReleaseState.Published);
        DtSummaryRecord dt = mock(DtSummaryRecord.class);
        CodeListSummaryRecord releaseCodeList = mock(CodeListSummaryRecord.class);

        when(repositoryFactory.codeListQueryRepository(requester)).thenReturn(codeListQuery);
        when(repositoryFactory.dtQueryRepository(requester)).thenReturn(dtQuery);
        when(codeListQuery.availableCodeListByDtManifestId(dtManifestId,
                List.of(CcState.Published, CcState.Production))).thenReturn(List.of());
        when(dtQuery.getDtSummary(dtManifestId)).thenReturn(dt);
        when(dt.release()).thenReturn(release);
        when(codeListQuery.getCodeListSummaryList(releaseId)).thenReturn(List.of(releaseCodeList));
        when(releaseCodeList.state()).thenReturn(CcState.Published);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableCodeListListByDtManifestId(requester, dtManifestId))
                .containsExactly(releaseCodeList);
        verify(codeListQuery).getCodeListSummaryList(releaseId);
    }

    @Test
    void keepsExplicitCodeListAvailabilityWithoutReleaseFallback() {
        CodeListQueryService service = new CodeListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        CodeListQueryRepository codeListQuery = mock(CodeListQueryRepository.class);
        ScoreUser requester = developer();
        DtManifestId dtManifestId = new DtManifestId(BigInteger.ONE);
        CodeListSummaryRecord explicitCodeList = mock(CodeListSummaryRecord.class);

        when(repositoryFactory.codeListQueryRepository(requester)).thenReturn(codeListQuery);
        when(codeListQuery.availableCodeListByDtManifestId(dtManifestId,
                List.of(CcState.Published, CcState.Production))).thenReturn(List.of(explicitCodeList));
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableCodeListListByDtManifestId(requester, dtManifestId))
                .containsExactly(explicitCodeList);
        verify(repositoryFactory, never()).dtQueryRepository(any());
    }

    @Test
    void doesNotFallBackWhenExplicitCodeListRelationHasNoVisibleCandidates() {
        CodeListQueryService service = new CodeListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        CodeListQueryRepository codeListQuery = mock(CodeListQueryRepository.class);
        ScoreUser requester = developer();
        DtManifestId dtManifestId = new DtManifestId(BigInteger.ONE);

        when(repositoryFactory.codeListQueryRepository(requester)).thenReturn(codeListQuery);
        when(codeListQuery.availableCodeListByDtManifestId(dtManifestId,
                List.of(CcState.Published, CcState.Production))).thenReturn(List.of());
        when(codeListQuery.hasCodeListAvailabilityByDtManifestId(dtManifestId)).thenReturn(true);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableCodeListListByDtManifestId(requester, dtManifestId)).isEmpty();
        verify(repositoryFactory, never()).dtQueryRepository(any());
    }

    @Test
    void fallsBackToAllCodeListsInTheReleaseWhenTheDtScHasNoExplicitCodeList() {
        CodeListQueryService service = new CodeListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        CodeListQueryRepository codeListQuery = mock(CodeListQueryRepository.class);
        DtQueryRepository dtQuery = mock(DtQueryRepository.class);
        ScoreUser requester = developer();
        DtScManifestId dtScManifestId = new DtScManifestId(BigInteger.ONE);
        ReleaseId releaseId = new ReleaseId(BigInteger.TEN);
        ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                releaseId, null, "10.9", ReleaseState.Published);
        DtScSummaryRecord dtSc = mock(DtScSummaryRecord.class);
        CodeListSummaryRecord releaseCodeList = mock(CodeListSummaryRecord.class);

        when(repositoryFactory.codeListQueryRepository(requester)).thenReturn(codeListQuery);
        when(repositoryFactory.dtQueryRepository(requester)).thenReturn(dtQuery);
        when(codeListQuery.availableCodeListByDtScManifestId(dtScManifestId,
                List.of(CcState.Published, CcState.Production))).thenReturn(List.of());
        when(dtQuery.getDtScSummary(dtScManifestId)).thenReturn(dtSc);
        when(dtSc.release()).thenReturn(release);
        when(codeListQuery.getCodeListSummaryList(releaseId)).thenReturn(List.of(releaseCodeList));
        when(releaseCodeList.state()).thenReturn(CcState.Published);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableCodeListListByDtScManifestId(requester, dtScManifestId))
                .containsExactly(releaseCodeList);
        verify(codeListQuery).getCodeListSummaryList(releaseId);
    }

    @Test
    void doesNotFallBackWhenExplicitCodeListDtScRelationHasNoVisibleCandidates() {
        CodeListQueryService service = new CodeListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        CodeListQueryRepository codeListQuery = mock(CodeListQueryRepository.class);
        ScoreUser requester = developer();
        DtScManifestId dtScManifestId = new DtScManifestId(BigInteger.ONE);

        when(repositoryFactory.codeListQueryRepository(requester)).thenReturn(codeListQuery);
        when(codeListQuery.availableCodeListByDtScManifestId(dtScManifestId,
                List.of(CcState.Published, CcState.Production))).thenReturn(List.of());
        when(codeListQuery.hasCodeListAvailabilityByDtScManifestId(dtScManifestId)).thenReturn(true);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableCodeListListByDtScManifestId(requester, dtScManifestId)).isEmpty();
        verify(repositoryFactory, never()).dtQueryRepository(any());
    }

    private static ScoreUser developer() {
        return new ScoreUser(null, "developer", "Developer", null,
                true, List.of(ScoreRole.DEVELOPER));
    }
}
