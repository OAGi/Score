package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository;
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
import static org.mockito.Mockito.*;

class AgencyIdListQueryServiceTest {

    @Test
    void filtersPreloadedAgencyIdListsWithoutTouchingTheDatabase() {
        AgencyIdListQueryService service = new AgencyIdListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        ScoreUser requester = developer();
        DtScManifestId dtScManifestId = new DtScManifestId(BigInteger.ONE);
        AgencyIdListManifestId allowedId = new AgencyIdListManifestId(BigInteger.TEN);
        AgencyIdListSummaryRecord allowed = mock(AgencyIdListSummaryRecord.class);
        AgencyIdListSummaryRecord disallowed = mock(AgencyIdListSummaryRecord.class);
        when(allowed.agencyIdListManifestId()).thenReturn(allowedId);
        when(disallowed.agencyIdListManifestId()).thenReturn(
                new AgencyIdListManifestId(BigInteger.valueOf(11)));
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableAgencyIdListListByDtScManifestId(
                requester, dtScManifestId, List.of(allowed, disallowed), Set.of(allowedId)))
                .containsExactly(allowed);
        verifyNoInteractions(repositoryFactory);
    }

    @Test
    void fallsBackToAllAgencyIdListsInTheReleaseWhenTheDtHasNoExplicitAgencyIdList() {
        AgencyIdListQueryService service = new AgencyIdListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        AgencyIdListQueryRepository agencyIdListQuery = mock(AgencyIdListQueryRepository.class);
        DtQueryRepository dtQuery = mock(DtQueryRepository.class);
        ScoreUser requester = developer();
        DtManifestId dtManifestId = new DtManifestId(BigInteger.ONE);
        ReleaseId releaseId = new ReleaseId(BigInteger.TEN);
        ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                releaseId, null, "10.9", ReleaseState.Published);
        DtSummaryRecord dt = mock(DtSummaryRecord.class);
        AgencyIdListSummaryRecord releaseAgencyIdList = mock(AgencyIdListSummaryRecord.class);

        when(repositoryFactory.agencyIdListQueryRepository(requester)).thenReturn(agencyIdListQuery);
        when(repositoryFactory.dtQueryRepository(requester)).thenReturn(dtQuery);
        when(agencyIdListQuery.availableAgencyIdListByDtManifestId(dtManifestId,
                List.of(CcState.Published, CcState.Production))).thenReturn(List.of());
        when(dtQuery.getDtSummary(dtManifestId)).thenReturn(dt);
        when(dt.release()).thenReturn(release);
        when(agencyIdListQuery.getAgencyIdListSummaryList(releaseId)).thenReturn(List.of(releaseAgencyIdList));
        when(releaseAgencyIdList.state()).thenReturn(CcState.Production);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableAgencyIdListListByDtManifestId(requester, dtManifestId))
                .containsExactly(releaseAgencyIdList);
        verify(agencyIdListQuery).getAgencyIdListSummaryList(releaseId);
    }

    @Test
    void fallsBackToAllAgencyIdListsInTheReleaseWhenTheDtScHasNoExplicitAgencyIdList() {
        AgencyIdListQueryService service = new AgencyIdListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        AgencyIdListQueryRepository agencyIdListQuery = mock(AgencyIdListQueryRepository.class);
        DtQueryRepository dtQuery = mock(DtQueryRepository.class);
        ScoreUser requester = developer();
        DtScManifestId dtScManifestId = new DtScManifestId(BigInteger.ONE);
        ReleaseId releaseId = new ReleaseId(BigInteger.TEN);
        ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                releaseId, null, "10.9", ReleaseState.Published);
        DtScSummaryRecord dtSc = mock(DtScSummaryRecord.class);
        AgencyIdListSummaryRecord releaseAgencyIdList = mock(AgencyIdListSummaryRecord.class);

        when(repositoryFactory.agencyIdListQueryRepository(requester)).thenReturn(agencyIdListQuery);
        when(repositoryFactory.dtQueryRepository(requester)).thenReturn(dtQuery);
        when(agencyIdListQuery.availableAgencyIdListByDtScManifestId(dtScManifestId,
                List.of(CcState.Published, CcState.Production))).thenReturn(List.of());
        when(dtQuery.getDtScSummary(dtScManifestId)).thenReturn(dtSc);
        when(dtSc.release()).thenReturn(release);
        when(agencyIdListQuery.getAgencyIdListSummaryList(releaseId)).thenReturn(List.of(releaseAgencyIdList));
        when(releaseAgencyIdList.state()).thenReturn(CcState.Production);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableAgencyIdListListByDtScManifestId(requester, dtScManifestId))
                .containsExactly(releaseAgencyIdList);
        verify(agencyIdListQuery).getAgencyIdListSummaryList(releaseId);
    }

    @Test
    void doesNotFallBackWhenExplicitAgencyRelationDtScHasNoVisibleCandidates() {
        AgencyIdListQueryService service = new AgencyIdListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        AgencyIdListQueryRepository agencyIdListQuery = mock(AgencyIdListQueryRepository.class);
        ScoreUser requester = developer();
        DtScManifestId dtScManifestId = new DtScManifestId(BigInteger.ONE);

        when(repositoryFactory.agencyIdListQueryRepository(requester)).thenReturn(agencyIdListQuery);
        when(agencyIdListQuery.availableAgencyIdListByDtScManifestId(dtScManifestId,
                List.of(CcState.Published, CcState.Production))).thenReturn(List.of());
        when(agencyIdListQuery.hasAgencyIdListAvailabilityByDtScManifestId(dtScManifestId)).thenReturn(true);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableAgencyIdListListByDtScManifestId(requester, dtScManifestId)).isEmpty();
        verify(repositoryFactory, never()).dtQueryRepository(any());
    }

    @Test
    void keepsExplicitAgencyIdListAvailabilityWithoutReleaseFallback() {
        AgencyIdListQueryService service = new AgencyIdListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        AgencyIdListQueryRepository agencyIdListQuery = mock(AgencyIdListQueryRepository.class);
        ScoreUser requester = developer();
        DtManifestId dtManifestId = new DtManifestId(BigInteger.ONE);
        AgencyIdListSummaryRecord explicitAgencyIdList = mock(AgencyIdListSummaryRecord.class);

        when(repositoryFactory.agencyIdListQueryRepository(requester)).thenReturn(agencyIdListQuery);
        when(agencyIdListQuery.availableAgencyIdListByDtManifestId(dtManifestId,
                List.of(CcState.Published, CcState.Production)))
                .thenReturn(List.of(explicitAgencyIdList));
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableAgencyIdListListByDtManifestId(requester, dtManifestId))
                .containsExactly(explicitAgencyIdList);
        verify(repositoryFactory, never()).dtQueryRepository(any());
    }

    @Test
    void doesNotFallBackWhenExplicitAgencyRelationHasNoVisibleCandidates() {
        AgencyIdListQueryService service = new AgencyIdListQueryService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        AgencyIdListQueryRepository agencyIdListQuery = mock(AgencyIdListQueryRepository.class);
        ScoreUser requester = developer();
        DtManifestId dtManifestId = new DtManifestId(BigInteger.ONE);

        when(repositoryFactory.agencyIdListQueryRepository(requester)).thenReturn(agencyIdListQuery);
        when(agencyIdListQuery.availableAgencyIdListByDtManifestId(dtManifestId,
                List.of(CcState.Published, CcState.Production))).thenReturn(List.of());
        when(agencyIdListQuery.hasAgencyIdListAvailabilityByDtManifestId(dtManifestId)).thenReturn(true);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        assertThat(service.availableAgencyIdListListByDtManifestId(requester, dtManifestId)).isEmpty();
        verify(repositoryFactory, never()).dtQueryRepository(any());
    }

    private static ScoreUser developer() {
        return new ScoreUser(null, "developer", "Developer", null,
                true, List.of(ScoreRole.DEVELOPER));
    }
}
