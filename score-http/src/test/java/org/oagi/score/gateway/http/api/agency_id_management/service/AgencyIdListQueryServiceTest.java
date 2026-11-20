package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.ValueListCandidate;
import org.oagi.score.gateway.http.common.model.Guid;
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
import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AgencyIdListQueryServiceTest {

    private static AgencyIdListManifestId id(int value) {
        return new AgencyIdListManifestId(BigInteger.valueOf(value));
    }

    private static ValueListCandidate<AgencyIdListManifestId> candidate(int id, Integer base, CcState state) {
        return new ValueListCandidate<>(id(id), new Guid(String.format("%032x", id)), "list" + id, "l" + id, "1", "agency",
                state, base == null ? null : id(base), false);
    }

    private static AgencyIdListSummaryRecord details(int id, String name) {
        AgencyIdListSummaryRecord record = mock(AgencyIdListSummaryRecord.class);
        when(record.agencyIdListManifestId()).thenReturn(id(id));
        when(record.name()).thenReturn(name);
        return record;
    }

    private static Set<AgencyIdListManifestId> ids(int... values) {
        Set<AgencyIdListManifestId> set = new LinkedHashSet<>();
        for (int value : values) {
            set.add(id(value));
        }
        return set;
    }

    private static final ReleaseId RELEASE = new ReleaseId(BigInteger.TEN);

    private final AgencyIdListQueryService service = new AgencyIdListQueryService();
    private final RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
    private final AgencyIdListQueryRepository agencyIdListQuery = mock(AgencyIdListQueryRepository.class);
    private final DtQueryRepository dtQuery = mock(DtQueryRepository.class);
    private final ScoreUser requester = developer();

    private void givenReleaseWith(List<ValueListCandidate<AgencyIdListManifestId>> candidates) {
        when(repositoryFactory.agencyIdListQueryRepository(requester)).thenReturn(agencyIdListQuery);
        when(repositoryFactory.dtQueryRepository(requester)).thenReturn(dtQuery);
        when(agencyIdListQuery.getAgencyIdListCandidateList(RELEASE)).thenReturn(candidates);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
    }

    private DtManifestId givenDt() {
        DtManifestId dtManifestId = new DtManifestId(BigInteger.ONE);
        DtSummaryRecord dt = mock(DtSummaryRecord.class);
        when(dtQuery.getDtSummary(dtManifestId)).thenReturn(dt);
        when(dt.release()).thenReturn(new ReleaseSummaryRecord(RELEASE, null, "10.9", ReleaseState.Published));
        return dtManifestId;
    }

    private DtScManifestId givenDtSc() {
        DtScManifestId dtScManifestId = new DtScManifestId(BigInteger.ONE);
        DtScSummaryRecord dtSc = mock(DtScSummaryRecord.class);
        when(dtQuery.getDtScSummary(dtScManifestId)).thenReturn(dtSc);
        when(dtSc.release()).thenReturn(new ReleaseSummaryRecord(RELEASE, null, "10.9", ReleaseState.Published));
        return dtScManifestId;
    }

    @Test
    void offersEveryAgencyIdListOfTheReleaseThatTheDeveloperCanSeeWhenTheDtNamesNone() {
        DtManifestId dt = givenDt();
        givenReleaseWith(List.of(candidate(1, null, CcState.Published), candidate(2, null, CcState.WIP),
                candidate(3, null, CcState.Production)));
        AgencyIdListSummaryRecord first = details(1, "b");
        AgencyIdListSummaryRecord third = details(3, "a");
        when(agencyIdListQuery.getNamedAgencyIdListManifestIdsByDtManifestId(dt)).thenReturn(Set.of());
        when(agencyIdListQuery.getAgencyIdListSummaryListByManifestIds(ids(1, 3))).thenReturn(List.of(third, first));

        // the whole release comes in its own order, not ordered by name
        assertThat(service.availableAgencyIdListListByDtManifestId(requester, dt)).containsExactly(first, third);
    }

    @Test
    void offersTheNamedAgencyIdListsWithTheListsDerivedFromThemButNotTheOnesTheyAreBasedOn() {
        DtManifestId dt = givenDt();
        givenReleaseWith(List.of(candidate(1, null, CcState.Published), candidate(2, 1, CcState.Published),
                candidate(3, 2, CcState.Published), candidate(4, null, CcState.Published)));
        AgencyIdListSummaryRecord middle = details(2, "z");
        AgencyIdListSummaryRecord leaf = details(3, "a");
        when(agencyIdListQuery.getNamedAgencyIdListManifestIdsByDtManifestId(dt)).thenReturn(ids(2));
        when(agencyIdListQuery.getAgencyIdListSummaryListByManifestIds(ids(2, 3))).thenReturn(List.of(middle, leaf));

        assertThat(service.availableAgencyIdListListByDtManifestId(requester, dt)).containsExactly(leaf, middle);
    }

    @Test
    void doesNotFallBackWhenTheNamedAgencyIdListsAreNotVisibleToTheDeveloper() {
        DtManifestId dt = givenDt();
        givenReleaseWith(List.of(candidate(1, null, CcState.WIP), candidate(2, null, CcState.Published)));
        when(agencyIdListQuery.getNamedAgencyIdListManifestIdsByDtManifestId(dt)).thenReturn(ids(1));

        assertThat(service.availableAgencyIdListListByDtManifestId(requester, dt)).isEmpty();
        verify(agencyIdListQuery, never()).getAgencyIdListSummaryListByManifestIds(any());
    }

    @Test
    void offersWhatTheDtScNamesAndFallsBackToTheReleaseWhenItNamesNone() {
        DtScManifestId dtSc = givenDtSc();
        givenReleaseWith(List.of(candidate(1, null, CcState.Published), candidate(2, null, CcState.Published)));
        AgencyIdListSummaryRecord second = details(2, "second");
        when(agencyIdListQuery.getNamedAgencyIdListManifestIdsByDtScManifestId(dtSc)).thenReturn(ids(2));
        when(agencyIdListQuery.getAgencyIdListSummaryListByManifestIds(ids(2))).thenReturn(List.of(second));

        assertThat(service.availableAgencyIdListListByDtScManifestId(requester, dtSc)).containsExactly(second);

        AgencyIdListSummaryRecord first = details(1, "first");
        when(agencyIdListQuery.getNamedAgencyIdListManifestIdsByDtScManifestId(dtSc)).thenReturn(Set.of());
        when(agencyIdListQuery.getAgencyIdListSummaryListByManifestIds(ids(1, 2))).thenReturn(List.of(first, second));

        assertThat(service.availableAgencyIdListListByDtScManifestId(requester, dtSc)).containsExactly(first, second);
    }

    @Test
    void doesNotFallBackWhenTheNamedAgencyIdListsOfTheDtScAreNotVisibleToTheDeveloper() {
        DtScManifestId dtSc = givenDtSc();
        givenReleaseWith(List.of(candidate(1, null, CcState.WIP), candidate(2, null, CcState.Published)));
        when(agencyIdListQuery.getNamedAgencyIdListManifestIdsByDtScManifestId(dtSc)).thenReturn(ids(1));

        assertThat(service.availableAgencyIdListListByDtScManifestId(requester, dtSc)).isEmpty();
    }

    private static ScoreUser developer() {
        return new ScoreUser(null, "developer", "Developer", null,
                true, List.of(ScoreRole.DEVELOPER));
    }
}
