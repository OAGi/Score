package org.oagi.score.gateway.http.api.release_management.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseProgressRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigInteger;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReleaseChangeNotifierTest {

    private final SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    private final RepositoryFactory repositories = mock(RepositoryFactory.class);
    private final ReleaseProgressRepository progress = mock(ReleaseProgressRepository.class);
    private final ReleaseChangeNotifier notifier = new ReleaseChangeNotifier(template, repositories);

    private final ReleaseId releaseId = new ReleaseId(BigInteger.valueOf(5));
    private final LibraryId libraryId = new LibraryId(BigInteger.valueOf(2));

    @AfterEach
    void clear() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void tellsAtOnceOutsideATransaction() {
        notifier.changed(libraryId, releaseId);

        verify(template).convertAndSend(eq(ReleaseChangeNotifier.TOPIC),
                eq(new ReleaseChangeNotifier.Change(libraryId, releaseId, "changed", null)));
    }

    @Test
    void tellsOnlyOnceTheTransactionHasCommitted() {
        TransactionSynchronizationManager.initSynchronization();

        notifier.changed(libraryId, releaseId);
        verifyNoInteractions(template);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(template).convertAndSend(eq(ReleaseChangeNotifier.TOPIC), any(Object.class));
    }

    @Test
    void tellsNothingWhenTheTransactionIsRolledBack() {
        TransactionSynchronizationManager.initSynchronization();

        notifier.changed(libraryId, releaseId);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verifyNoInteractions(template);
    }

    @Test
    void findsTheLibraryOfTheRelease() {
        when(repositories.releaseProgressRepository()).thenReturn(progress);
        when(progress.libraryOfRelease(releaseId)).thenReturn(Optional.of(libraryId));

        notifier.changed(releaseId);

        verify(template).convertAndSend(eq(ReleaseChangeNotifier.TOPIC),
                eq(new ReleaseChangeNotifier.Change(libraryId, releaseId, "changed", null)));
    }

    @Test
    void tellsWhoAskedForTheReleaseWhenItsWorkFailed() {
        when(repositories.releaseProgressRepository()).thenReturn(progress);
        when(progress.libraryOfRelease(releaseId)).thenReturn(Optional.of(libraryId));

        notifier.failed(releaseId, "alice");

        verify(template).convertAndSend(eq(ReleaseChangeNotifier.TOPIC),
                eq(new ReleaseChangeNotifier.Change(libraryId, releaseId, "failed", "alice")));
    }

    @Test
    void doesNotFailTheCallerWhenTheMessageCannotBeSent() {
        doThrow(new IllegalStateException("broker is down")).when(template).convertAndSend(anyString(), any(Object.class));

        notifier.changed(libraryId, releaseId);
    }
}
