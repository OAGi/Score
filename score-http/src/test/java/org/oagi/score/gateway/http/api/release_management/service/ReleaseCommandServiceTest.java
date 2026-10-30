package org.oagi.score.gateway.http.api.release_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceDetailsRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceQueryRepository;
import org.oagi.score.gateway.http.api.release_management.controller.payload.CreateReleaseRequest;
import org.oagi.score.gateway.http.api.release_management.controller.payload.UpdateReleaseRequest;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReleaseCommandServiceTest {

    @Test
    void rejectsCreateByANonDeveloper() {
        ScoreUser requester = user();
        ReleaseCommandRepository command = mock(ReleaseCommandRepository.class);
        ReleaseQueryRepository query = mock(ReleaseQueryRepository.class);
        ReleaseCommandService service = service(query, command);

        CreateReleaseRequest request = new CreateReleaseRequest(null, null, "2.0", null, null);

        assertThatThrownBy(() -> service.create(requester, request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(command, never()).create(any(), any(), any(), any(), any());
    }

    @Test
    void rejectsCreateWithANamespaceOfAnotherLibrary() {
        ScoreUser requester = user(ScoreRole.DEVELOPER);
        LibraryId libraryA = new LibraryId(BigInteger.ONE);
        LibraryId libraryB = new LibraryId(BigInteger.TWO);
        NamespaceId namespaceId = new NamespaceId(BigInteger.TEN);
        ReleaseCommandRepository command = mock(ReleaseCommandRepository.class);
        ReleaseQueryRepository query = mock(ReleaseQueryRepository.class);
        NamespaceQueryRepository namespaceQuery = mock(NamespaceQueryRepository.class);
        NamespaceDetailsRecord namespace = mock(NamespaceDetailsRecord.class);
        when(namespace.libraryId()).thenReturn(libraryA);
        when(namespaceQuery.getNamespaceDetails(any(), any())).thenReturn(namespace);
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.releaseQueryRepository(any())).thenReturn(query);
        when(repositories.releaseCommandRepository(any())).thenReturn(command);
        when(repositories.namespaceQueryRepository(any())).thenReturn(namespaceQuery);
        ReleaseCommandService service = new ReleaseCommandService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);

        CreateReleaseRequest request = new CreateReleaseRequest(libraryB, namespaceId, "2.0", null, null);

        assertThatThrownBy(() -> service.create(requester, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("namespace");
        verify(command, never()).create(any(), any(), any(), any(), any());
    }

    @Test
    void rejectsUpdateByANonDeveloper() {
        ScoreUser requester = user();
        ReleaseId releaseId = id(1);
        ReleaseCommandRepository command = mock(ReleaseCommandRepository.class);
        ReleaseQueryRepository query = mock(ReleaseQueryRepository.class);
        when(query.exists(releaseId)).thenReturn(true);
        when(query.getReleaseSummary(releaseId)).thenReturn(summary(releaseId, "1.0", ReleaseState.Initialized));
        ReleaseCommandService service = service(query, command);

        UpdateReleaseRequest request = new UpdateReleaseRequest(releaseId, null, "1.1", null, null);

        assertThatThrownBy(() -> service.update(requester, request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(command, never()).update(any(), any(), any(), any(), any());
    }

    @Test
    void rejectsUpdateOfAPublishedRelease() {
        ScoreUser requester = user(ScoreRole.DEVELOPER);
        ReleaseId releaseId = id(1);
        ReleaseCommandRepository command = mock(ReleaseCommandRepository.class);
        ReleaseQueryRepository query = mock(ReleaseQueryRepository.class);
        when(query.exists(releaseId)).thenReturn(true);
        when(query.getReleaseSummary(releaseId)).thenReturn(summary(releaseId, "Working", ReleaseState.Published));
        ReleaseCommandService service = service(query, command);

        UpdateReleaseRequest request = new UpdateReleaseRequest(releaseId, null, "2.0", null, null);

        assertThatThrownBy(() -> service.update(requester, request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(command, never()).update(any(), any(), any(), any(), any());
    }

    @Test
    void allowsUpdateOfADraftReleaseByADeveloper() {
        ScoreUser requester = user(ScoreRole.DEVELOPER);
        ReleaseId releaseId = id(1);
        ReleaseCommandRepository command = mock(ReleaseCommandRepository.class);
        ReleaseQueryRepository query = mock(ReleaseQueryRepository.class);
        when(query.exists(releaseId)).thenReturn(true);
        when(query.getReleaseSummary(releaseId)).thenReturn(summary(releaseId, "1.0", ReleaseState.Draft));
        ReleaseCommandService service = service(query, command);

        UpdateReleaseRequest request = new UpdateReleaseRequest(releaseId, null, "1.1", null, null);

        service.update(requester, request);

        verify(command).update(releaseId, null, "1.1", null, null);
    }

    @Test
    void rejectsCreateWithABlankReleaseNumber() {
        ScoreUser requester = user(ScoreRole.DEVELOPER);
        ReleaseCommandRepository command = mock(ReleaseCommandRepository.class);
        ReleaseCommandService service = service(mock(ReleaseQueryRepository.class), command);

        CreateReleaseRequest request = new CreateReleaseRequest(null, null, "   ", null, null);

        assertThatThrownBy(() -> service.create(requester, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("releaseNum");
        verify(command, never()).create(any(), any(), any(), any(), any());
    }

    @Test
    void rejectsUpdateWithABlankReleaseNumber() {
        ScoreUser requester = user(ScoreRole.DEVELOPER);
        ReleaseId releaseId = id(1);
        ReleaseCommandRepository command = mock(ReleaseCommandRepository.class);
        ReleaseQueryRepository query = mock(ReleaseQueryRepository.class);
        when(query.getReleaseSummary(releaseId)).thenReturn(summary(releaseId, "1.0", ReleaseState.Draft));
        ReleaseCommandService service = service(query, command);

        UpdateReleaseRequest request = new UpdateReleaseRequest(releaseId, null, " ", null, null);

        assertThatThrownBy(() -> service.update(requester, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("releaseNum");
        verify(command, never()).update(any(), any(), any(), any(), any());
    }

    private ReleaseCommandService service(ReleaseQueryRepository query, ReleaseCommandRepository command) {
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.releaseQueryRepository(any())).thenReturn(query);
        when(repositories.releaseCommandRepository(any())).thenReturn(command);
        ReleaseCommandService service = new ReleaseCommandService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
        return service;
    }

    private ReleaseSummaryRecord summary(ReleaseId releaseId, String releaseNum, ReleaseState state) {
        return new ReleaseSummaryRecord(releaseId, null, releaseNum, state);
    }

    private ScoreUser user(ScoreRole... roles) {
        List<ScoreRole> roleList = roles == null ? Collections.emptyList() : List.of(roles);
        return new ScoreUser(new UserId(BigInteger.ONE), "user", "User", "user@example.com", true, roleList);
    }

    private ReleaseId id(int value) {
        return new ReleaseId(BigInteger.valueOf(value));
    }
}
