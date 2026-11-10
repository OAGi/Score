package org.oagi.score.gateway.http.common.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.exception_management.model.ExceptionId;
import org.oagi.score.gateway.http.api.exception_management.repository.ExceptionCommandRepository;
import org.oagi.score.gateway.http.api.message_management.service.MessageCommandService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.configuration.websocket.WebSocketAuthorizationInterceptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigInteger;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AsyncJobFailureReporterTest {

    private final RepositoryFactory repositories = mock(RepositoryFactory.class);
    private final ExceptionCommandRepository exceptions = mock(ExceptionCommandRepository.class);
    private final SessionService sessions = mock(SessionService.class);
    private final MessageCommandService messages = mock(MessageCommandService.class);
    private final SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    private final AsyncJobFailureReporter reporter =
            new AsyncJobFailureReporter(repositories, sessions, messages, new StompNotifier(template));

    private final UserId requesterId = new UserId(BigInteger.valueOf(7));
    private final ScoreUser system = mock(ScoreUser.class);
    private final ScoreUser requester = mock(ScoreUser.class);

    AsyncJobFailureReporterTest() {
        when(sessions.getScoreSystemUser()).thenReturn(system);
        when(sessions.getScoreUserByUserId(requesterId)).thenReturn(requester);
        when(requester.username()).thenReturn("alice");
        when(repositories.exceptionCommandRepository(system)).thenReturn(exceptions);
        when(exceptions.create(anyString(), anyString(), anyString(), any())).thenReturn(mock(ExceptionId.class));
    }

    @AfterEach
    void clear() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private AsyncJobFailureReporter.Report report(Object userEvent) {
        return new AsyncJobFailureReporter.Report("copy.bie", "12", requesterId, "technical", "details",
                "subject", "body", userEvent);
    }

    @Test
    void keepsTheRecordAndTellsTheUserAloneOnceTheTransactionHasCommitted() {
        TransactionSynchronizationManager.initSynchronization();

        reporter.report(report("event"));

        verify(exceptions).create("copy.bie#12", "technical", "details", requesterId);
        verifyNoInteractions(template, messages);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        verify(template).convertAndSend(eq(WebSocketAuthorizationInterceptor.messageTopicOf("alice")), eq((Object) "event"));
        verify(messages).asyncSendMessage(system, List.of(requesterId), "subject", "body", "text/markdown");
    }

    @Test
    void tellsNoOneWhenTheTransactionIsRolledBack() {
        TransactionSynchronizationManager.initSynchronization();

        reporter.report(report("event"));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verifyNoInteractions(template, messages);
    }

    @Test
    void sendsNoEventWhenTheWorkHasNone() {
        reporter.report(report(null));

        verifyNoInteractions(template);
        verify(messages).asyncSendMessage(any(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void doesNotFailWhenTheMessageCannotBeSent() {
        when(messages.asyncSendMessage(any(), any(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalStateException("down"));

        reporter.report(report(null));
    }
}
