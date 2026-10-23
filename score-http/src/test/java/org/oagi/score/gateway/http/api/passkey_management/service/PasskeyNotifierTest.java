package org.oagi.score.gateway.http.api.passkey_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.message_management.service.MessageCommandService;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasskeyNotifierTest {

    private static final UserId ALICE = new UserId(BigInteger.TEN);

    private final ScoreUser system = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "System", null, true, List.of());
    private final PasskeyQueryRepository query = mock(PasskeyQueryRepository.class);
    private final MessageCommandService messages = mock(MessageCommandService.class);
    private PasskeyNotifier notifier;

    @BeforeEach
    void setUp() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.passkeyQueryRepository(any())).thenReturn(query);
        SessionService sessionService = mock(SessionService.class);
        when(sessionService.getScoreSystemUser()).thenReturn(system);

        notifier = new PasskeyNotifier();
        ReflectionTestUtils.setField(notifier, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(notifier, "sessionService", sessionService);
        ReflectionTestUtils.setField(notifier, "messageCommandService", messages);
    }

    private ArgumentCaptor<String> sentBody(String expectedSubject) {
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<UserId>> recipients = ArgumentCaptor.forClass(Collection.class);
        verify(messages).asyncSendMessage(eq(system), recipients.capture(), eq(expectedSubject), body.capture(),
                eq("text/markdown"));
        assertEquals(List.of(ALICE), List.copyOf(recipients.getValue()));
        return body;
    }

    @Test
    void anAddedPasskeyIsReportedWithTheNumberTheUserHasNow() {
        when(query.countCredentialsByUserId(ALICE)).thenReturn(2);

        notifier.passkeyAdded(ALICE);

        String body = sentBody("A passkey was added to your account").getValue();
        assertTrue(body.contains("You have 2 passkeys now."), body);
        assertTrue(body.contains("(/settings/account/passkeys)"), body);
    }

    @Test
    void theNumberIsWordedForOneAndForNone() {
        assertEquals("You have 1 passkey now.", PasskeyNotifier.have(1));
        assertEquals("You have no passkeys now.", PasskeyNotifier.have(0));
    }

    @Test
    void aRemovedPasskeyIsReported() {
        when(query.countCredentialsByUserId(ALICE)).thenReturn(1);

        notifier.passkeyRemoved(ALICE);

        assertTrue(sentBody("A passkey was removed from your account").getValue().contains("You have 1 passkey now."));
    }

    @Test
    void anAdministratorRemovalSaysHowManyWereRemoved() {
        when(query.countCredentialsByUserId(ALICE)).thenReturn(0);

        notifier.passkeysRemovedByAdministrator(ALICE, 3);

        String body = sentBody("An administrator removed passkeys from your account").getValue();
        assertTrue(body.contains("removed 3 passkeys"), body);
        assertTrue(body.contains("You have no passkeys now."), body);
    }

    @Test
    void nothingIsSentWhenAnAdministratorRemovedNothing() {
        notifier.passkeysRemovedByAdministrator(ALICE, 0);

        verify(messages, never()).asyncSendMessage(any(), any(), any(), any(), any());
    }

    @Test
    void aMessageThatCannotBeSentNeverFailsTheChange() {
        when(query.countCredentialsByUserId(ALICE)).thenThrow(new IllegalStateException("database is down"));

        notifier.passkeyAdded(ALICE);

        verify(messages, never()).asyncSendMessage(any(), any(), any(), any(), any());
    }

}
