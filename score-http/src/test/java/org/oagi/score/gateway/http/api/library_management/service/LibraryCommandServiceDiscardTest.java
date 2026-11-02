package org.oagi.score.gateway.http.api.library_management.service;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryCommandRepository;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetQueryRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceCommandRepository;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceQueryRepository;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LibraryCommandServiceDiscardTest {

    @Test
    void discardDeletesTheNamespacesOfTheLibraryBeforeTheLibrary() {
        LibraryId libraryId = new LibraryId(BigInteger.ONE);
        NamespaceId namespaceA = new NamespaceId(BigInteger.TWO);
        NamespaceId namespaceB = new NamespaceId(BigInteger.TEN);
        ScoreUser requester =
                new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", null, false, List.of(ScoreRole.ADMINISTRATOR));

        LibraryQueryRepository libraryQuery = mock(LibraryQueryRepository.class);
        when(libraryQuery.exists(libraryId)).thenReturn(true);
        LibraryCommandRepository libraryCommand = mock(LibraryCommandRepository.class);
        when(libraryCommand.delete(libraryId)).thenReturn(true);
        ReleaseQueryRepository releaseQuery = mock(ReleaseQueryRepository.class);
        when(releaseQuery.getReleaseSummaryList(libraryId)).thenReturn(List.of());
        ModuleSetQueryRepository moduleSetQuery = mock(ModuleSetQueryRepository.class);
        when(moduleSetQuery.getModuleSetSummaryList(libraryId)).thenReturn(List.of());
        NamespaceQueryRepository namespaceQuery = mock(NamespaceQueryRepository.class);
        when(namespaceQuery.getNamespaceSummaryList(libraryId)).thenReturn(List.of(
                new NamespaceSummaryRecord(namespaceA, "urn:a", "a", true),
                new NamespaceSummaryRecord(namespaceB, "urn:b", "b", false)));
        NamespaceCommandRepository namespaceCommand = mock(NamespaceCommandRepository.class);

        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.libraryQueryRepository(any())).thenReturn(libraryQuery);
        when(repositories.libraryCommandRepository(any())).thenReturn(libraryCommand);
        when(repositories.releaseQueryRepository(any())).thenReturn(releaseQuery);
        when(repositories.moduleSetQueryRepository(any())).thenReturn(moduleSetQuery);
        when(repositories.moduleSetCommandRepository(any())).thenReturn(mock(ModuleSetCommandRepository.class));
        when(repositories.namespaceQueryRepository(any())).thenReturn(namespaceQuery);
        when(repositories.namespaceCommandRepository(any())).thenReturn(namespaceCommand);
        LibraryCommandService service = new LibraryCommandService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);

        service.discard(requester, libraryId);

        InOrder order = inOrder(namespaceCommand, libraryCommand);
        order.verify(namespaceCommand).delete(namespaceA);
        order.verify(namespaceCommand).delete(namespaceB);
        order.verify(libraryCommand).delete(libraryId);
    }
}
