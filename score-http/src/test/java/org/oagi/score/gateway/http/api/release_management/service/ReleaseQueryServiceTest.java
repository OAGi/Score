package org.oagi.score.gateway.http.api.release_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReleaseQueryServiceTest {

    @Test
    void followsReleaseChainInsteadOfComparingReleaseIds() {
        ReleaseId source = id(100);
        ReleaseId middle = id(10);
        ReleaseId target = id(2);
        ReleaseQueryRepository repository = mock(ReleaseQueryRepository.class);
        when(repository.getReleaseDetails(source)).thenReturn(details(next(middle)));
        when(repository.getReleaseDetails(middle)).thenReturn(details(next(target)));

        ReleaseQueryService service = service(repository);

        assertThat(service.isLaterRelease(mock(ScoreUser.class), source, target)).isTrue();
    }

    @Test
    void rejectsReleaseOutsideTheSourceChain() {
        ReleaseId source = id(100);
        ReleaseId next = id(10);
        ReleaseQueryRepository repository = mock(ReleaseQueryRepository.class);
        when(repository.getReleaseDetails(source)).thenReturn(details(next(next)));
        when(repository.getReleaseDetails(next)).thenReturn(details(null));

        ReleaseQueryService service = service(repository);

        assertThat(service.isLaterRelease(mock(ScoreUser.class), source, id(2))).isFalse();
    }

    @Test
    void stopsOnCyclicReleaseChain() {
        ReleaseId source = id(100);
        ReleaseId next = id(10);
        ReleaseQueryRepository repository = mock(ReleaseQueryRepository.class);
        when(repository.getReleaseDetails(source)).thenReturn(details(next(next)));
        when(repository.getReleaseDetails(next)).thenReturn(details(next(source)));

        ReleaseQueryService service = service(repository);

        assertThat(service.isLaterRelease(mock(ScoreUser.class), source, id(2))).isFalse();
    }

    @Test
    void anUnknownReleaseIsNotFoundWhereItsAssignableComponentsAreAskedFor() {
        ReleaseQueryRepository repository = mock(ReleaseQueryRepository.class);
        when(repository.getReleaseSummary(id(99999))).thenReturn(null);

        assertThatThrownBy(() -> service(repository).getAssignComponents(mock(ScoreUser.class), id(99999)))
                .isInstanceOf(NotFoundException.class);
        verify(repository, never()).getAssignComponents(any());
    }

    @Test
    void anUnknownReleaseIsNotFoundWhereItsMigrationScriptIsGenerated() {
        ReleaseQueryRepository repository = mock(ReleaseQueryRepository.class);
        when(repository.getReleaseSummary(id(99999))).thenReturn(null);

        assertThatThrownBy(() -> service(repository).generateMigrationScript(mock(ScoreUser.class), id(99999), false))
                .isInstanceOf(NotFoundException.class);
    }

    private ReleaseQueryService service(ReleaseQueryRepository repository) {
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.releaseQueryRepository(any())).thenReturn(repository);
        ReleaseQueryService service = new ReleaseQueryService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
        return service;
    }

    private ReleaseDetailsRecord details(ReleaseSummaryRecord next) {
        return new ReleaseDetailsRecord(null, null, null, null, null,
                null, null, null, null, null, null, next, false);
    }

    private ReleaseSummaryRecord next(ReleaseId releaseId) {
        return new ReleaseSummaryRecord(releaseId, null, null, null);
    }

    private ReleaseId id(int value) {
        return new ReleaseId(BigInteger.valueOf(value));
    }
}
