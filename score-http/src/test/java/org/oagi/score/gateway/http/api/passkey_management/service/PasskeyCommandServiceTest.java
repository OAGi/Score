package org.oagi.score.gateway.http.api.passkey_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyCommandRepository;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyQueryRepository;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The rules around removing and renaming passkeys: only the owner may, a passkey that is not yours looks missing,
 * the last sign-in method cannot be removed, and the bulk actions are administrator-only.
 */
class PasskeyCommandServiceTest {

    private static final UserId ALICE = new UserId(BigInteger.valueOf(10));
    private static final UserId BOB = new UserId(BigInteger.valueOf(20));
    private static final byte[] CREDENTIAL_ID = {1, 2, 3};
    private static final String ID = Base64.getUrlEncoder().withoutPadding().encodeToString(CREDENTIAL_ID);

    private final ScoreUser alice = new ScoreUser(ALICE, "alice", "Alice", "a@example.com", true, List.of(ScoreRole.END_USER));
    private final ScoreUser admin = new ScoreUser(new UserId(BigInteger.valueOf(1)), "admin", "Admin",
            "admin@example.com", true, List.of(ScoreRole.ADMINISTRATOR));

    private PasskeyQueryRepository query;
    private PasskeyCommandRepository command;
    private SessionService sessionService;
    private PasskeyProperties passkeyProperties;
    private PasskeyCommandService service;

    @BeforeEach
    void setUp() {
        query = mock(PasskeyQueryRepository.class);
        command = mock(PasskeyCommandRepository.class);
        sessionService = mock(SessionService.class);
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.passkeyQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.passkeyCommandRepository(any())).thenReturn(command);

        service = new PasskeyCommandService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "sessionService", sessionService);
    }

    private void useConfiguration(PasskeyProperties properties) {
        ReflectionTestUtils.setField(service, "passkeyProperties", properties);
    }

    private static PasskeyCredential credentialOf(UserId owner) {
        LocalDateTime now = LocalDateTime.of(2026, 10, 8, 9, 0);
        return new PasskeyCredential(CREDENTIAL_ID, new byte[]{9}, owner, "localhost", new byte[]{2}, 0, true, true,
                false, null, "public-key", null, null, "MacBook", now, now, null, null);
    }

    private void aliceOwnsThePasskey() {
        when(query.findCredential(CREDENTIAL_ID)).thenReturn(Optional.of(credentialOf(ALICE)));
    }

    @Test
    void ownerRenamesAPasskey() {
        aliceOwnsThePasskey();
        service.renamePasskey(alice, ID, "  Work laptop ");
        verify(command).updateLabel(CREDENTIAL_ID, "Work laptop");
    }

    @Test
    void renameNeedsAName() {
        assertThrows(IllegalArgumentException.class, () -> service.renamePasskey(alice, ID, "  "));
        assertThrows(IllegalArgumentException.class, () -> service.renamePasskey(alice, ID, null));
        verify(command, never()).updateLabel(any(), anyString());
    }

    @Test
    void anotherUsersPasskeyLooksMissing() {
        when(query.findCredential(CREDENTIAL_ID)).thenReturn(Optional.of(credentialOf(BOB)));

        assertThrows(NotFoundException.class, () -> service.renamePasskey(alice, ID, "x"));
        assertThrows(NotFoundException.class, () -> service.deleteMyPasskey(alice, ID));
        verify(command, never()).updateLabel(any(), anyString());
        verify(command, never()).deleteCredential(any());
    }

    @Test
    void unknownPasskeyIsNotFound() {
        when(query.findCredential(CREDENTIAL_ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.deleteMyPasskey(alice, ID));
    }

    @Test
    void malformedIdIsRejectedBeforeAnyLookup() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteMyPasskey(alice, "not base64 !!"));
        verify(query, never()).findCredential(any());
    }

    @Test
    void ownerRemovesOneOfSeveralPasskeys() {
        aliceOwnsThePasskey();
        when(query.countCredentialsByUserId(ALICE)).thenReturn(2);
        when(query.hasPassword(ALICE)).thenReturn(false);

        service.deleteMyPasskey(alice, ID);

        verify(command).deleteCredential(CREDENTIAL_ID);
    }

    @Test
    void ownerWithAPasswordMayRemoveTheLastPasskey() {
        aliceOwnsThePasskey();
        when(query.countCredentialsByUserId(ALICE)).thenReturn(1);
        when(query.hasPassword(ALICE)).thenReturn(true);

        service.deleteMyPasskey(alice, ID);

        verify(command).deleteCredential(CREDENTIAL_ID);
    }

    @Test
    void theOnlySignInMethodCannotBeRemoved() {
        aliceOwnsThePasskey();
        when(query.countCredentialsByUserId(ALICE)).thenReturn(1);
        when(query.hasPassword(ALICE)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.deleteMyPasskey(alice, ID));

        assertEquals("This is your only sign-in method. Set a password before removing your last passkey.", ex.getMessage());
        verify(command, never()).deleteCredential(any());
    }

    @Test
    void administratorRemovesAUsersPasskeysAndEndsTheirSessions() {
        when(query.findLoginId(ALICE)).thenReturn(Optional.of("alice"));
        when(command.deleteCredentialsByUserId(ALICE)).thenReturn(2);

        int removed = service.deletePasskeysOfUser(admin, ALICE, true);

        assertEquals(2, removed);
        verify(sessionService).invalidateByUsername("alice");
    }

    @Test
    void administratorMayKeepTheSessions() {
        when(query.findLoginId(ALICE)).thenReturn(Optional.of("alice"));

        service.deletePasskeysOfUser(admin, ALICE, false);

        verify(sessionService, never()).invalidateByUsername(anyString());
    }

    @Test
    void removingTheirPasskeysForAnUnknownUserIsNotFound() {
        when(query.findLoginId(ALICE)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.deletePasskeysOfUser(admin, ALICE, true));
        verify(command, never()).deleteCredentialsByUserId(any());
    }

    @Test
    void ordinaryUsersCannotRemoveOthersPasskeys() {
        assertThrows(AccessControlException.class, () -> service.deletePasskeysOfUser(alice, BOB, true));
        assertThrows(AccessControlException.class, () -> service.deleteStalePasskeys(alice));
        verify(command, never()).deleteCredentialsByUserId(any());
        verify(command, never()).deleteCredentialsOutsideRpId(anyString());
    }

    @Test
    void stalePasskeysAreThoseOutsideTheConfiguredRpId() {
        useConfiguration(new PasskeyProperties("score.example.org", "", "https://score.example.org"));
        when(command.deleteCredentialsOutsideRpId("score.example.org")).thenReturn(5);

        assertEquals(5, service.deleteStalePasskeys(admin));
    }

    @Test
    void stalePasskeyCleanupIsRefusedWhileNotConfigured() {
        useConfiguration(new PasskeyProperties("", "", ""));

        assertThrows(IllegalArgumentException.class, () -> service.deleteStalePasskeys(admin));
        verify(command, never()).deleteCredentialsOutsideRpId(anyString());
    }

}
