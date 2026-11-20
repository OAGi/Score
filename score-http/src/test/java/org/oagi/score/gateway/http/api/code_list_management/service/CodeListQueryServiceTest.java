package org.oagi.score.gateway.http.api.code_list_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.ValueListCandidate;
import org.oagi.score.gateway.http.common.model.Guid;
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
import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CodeListQueryServiceTest {

    private static CodeListManifestId id(int value) {
        return new CodeListManifestId(BigInteger.valueOf(value));
    }

    private static ValueListCandidate<CodeListManifestId> candidate(int id, Integer base, CcState state) {
        return new ValueListCandidate<>(id(id), new Guid(String.format("%032x", id)), "list" + id, "l" + id, "1", null,
                state, base == null ? null : id(base), false);
    }

    private static CodeListSummaryRecord details(int id, String name) {
        CodeListSummaryRecord record = mock(CodeListSummaryRecord.class);
        when(record.codeListManifestId()).thenReturn(id(id));
        when(record.name()).thenReturn(name);
        return record;
    }

    private static Set<CodeListManifestId> ids(int... values) {
        Set<CodeListManifestId> set = new LinkedHashSet<>();
        for (int value : values) {
            set.add(id(value));
        }
        return set;
    }

    private static final ReleaseId RELEASE = new ReleaseId(BigInteger.TEN);

    private final CodeListQueryService service = new CodeListQueryService();
    private final RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
    private final CodeListQueryRepository codeListQuery = mock(CodeListQueryRepository.class);
    private final DtQueryRepository dtQuery = mock(DtQueryRepository.class);
    private final ScoreUser requester = developer();

    private void givenReleaseWith(List<ValueListCandidate<CodeListManifestId>> candidates) {
        when(repositoryFactory.codeListQueryRepository(requester)).thenReturn(codeListQuery);
        when(repositoryFactory.dtQueryRepository(requester)).thenReturn(dtQuery);
        when(codeListQuery.getCodeListCandidateList(RELEASE)).thenReturn(candidates);
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
    void offersEveryCodeListOfTheReleaseThatTheDeveloperCanSeeWhenTheDtNamesNone() {
        DtManifestId dt = givenDt();
        givenReleaseWith(List.of(candidate(1, null, CcState.Published), candidate(2, null, CcState.WIP),
                candidate(3, null, CcState.Production)));
        CodeListSummaryRecord first = details(1, "b");
        CodeListSummaryRecord third = details(3, "a");
        when(codeListQuery.getNamedCodeListManifestIdsByDtManifestId(dt)).thenReturn(Set.of());
        when(codeListQuery.getCodeListSummaryListByManifestIds(ids(1, 3))).thenReturn(List.of(third, first));

        // the whole release comes in its own order, not ordered by name
        assertThat(service.availableCodeListListByDtManifestId(requester, dt)).containsExactly(first, third);
    }

    @Test
    void offersTheNamedCodeListsWithTheListsDerivedFromThemOrderedByNameAndNothingElse() {
        DtManifestId dt = givenDt();
        givenReleaseWith(List.of(candidate(1, null, CcState.Published), candidate(2, 1, CcState.Published),
                candidate(3, null, CcState.Published)));
        CodeListSummaryRecord base = details(1, "z");
        CodeListSummaryRecord derived = details(2, "a");
        when(codeListQuery.getNamedCodeListManifestIdsByDtManifestId(dt)).thenReturn(ids(1));
        when(codeListQuery.getCodeListSummaryListByManifestIds(ids(1, 2))).thenReturn(List.of(base, derived));

        assertThat(service.availableCodeListListByDtManifestId(requester, dt)).containsExactly(derived, base);
    }

    @Test
    void doesNotFallBackWhenTheNamedCodeListsAreNotVisibleToTheDeveloper() {
        DtManifestId dt = givenDt();
        givenReleaseWith(List.of(candidate(1, null, CcState.WIP), candidate(2, null, CcState.Published)));
        when(codeListQuery.getNamedCodeListManifestIdsByDtManifestId(dt)).thenReturn(ids(1));

        assertThat(service.availableCodeListListByDtManifestId(requester, dt)).isEmpty();
        verify(codeListQuery, never()).getCodeListSummaryListByManifestIds(any());
    }

    @Test
    void offersWhatTheDtScNamesAndFallsBackToTheReleaseWhenItNamesNone() {
        DtScManifestId named = givenDtSc();
        givenReleaseWith(List.of(candidate(1, null, CcState.Published), candidate(2, null, CcState.Published)));
        CodeListSummaryRecord second = details(2, "second");
        when(codeListQuery.getNamedCodeListManifestIdsByDtScManifestId(named)).thenReturn(ids(2));
        when(codeListQuery.getCodeListSummaryListByManifestIds(ids(2))).thenReturn(List.of(second));

        assertThat(service.availableCodeListListByDtScManifestId(requester, named)).containsExactly(second);

        CodeListSummaryRecord first = details(1, "first");
        when(codeListQuery.getNamedCodeListManifestIdsByDtScManifestId(named)).thenReturn(Set.of());
        when(codeListQuery.getCodeListSummaryListByManifestIds(ids(1, 2))).thenReturn(List.of(first, second));

        assertThat(service.availableCodeListListByDtScManifestId(requester, named)).containsExactly(first, second);
    }

    @Test
    void doesNotFallBackWhenTheNamedCodeListsOfTheDtScAreNotVisibleToTheDeveloper() {
        DtScManifestId dtSc = givenDtSc();
        givenReleaseWith(List.of(candidate(1, null, CcState.WIP), candidate(2, null, CcState.Published)));
        when(codeListQuery.getNamedCodeListManifestIdsByDtScManifestId(dtSc)).thenReturn(ids(1));

        assertThat(service.availableCodeListListByDtScManifestId(requester, dtSc)).isEmpty();
    }

    @Test
    void offersNothingForADtThatDoesNotExist() {
        givenReleaseWith(List.of(candidate(1, null, CcState.Published)));

        assertThat(service.availableCodeListListByDtManifestId(requester, new DtManifestId(BigInteger.TWO))).isEmpty();
        verify(codeListQuery, never()).getCodeListCandidateList(any());
    }

    private static ScoreUser developer() {
        return new ScoreUser(null, "developer", "Developer", null,
                true, List.of(ScoreRole.DEVELOPER));
    }
}
