package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.junit.jupiter.api.AfterEach;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyUser;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyCommandRepository;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyQueryRepository;
import org.oagi.score.gateway.http.api.passkey_management.service.PasskeyNotifier;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigInteger;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreUserCredentialRepositoryTest {

    @Test
    void transportsRoundTrip() {
        Set<AuthenticatorTransport> transports = new LinkedHashSet<>();
        transports.add(AuthenticatorTransport.INTERNAL);
        transports.add(AuthenticatorTransport.HYBRID);

        String joined = ScoreUserCredentialRepository.joinTransports(transports);

        assertEquals("internal,hybrid", joined);
        assertEquals(transports, ScoreUserCredentialRepository.splitTransports(joined));
    }

    @Test
    void noTransportsIsStoredAsNull() {
        assertNull(ScoreUserCredentialRepository.joinTransports(null));
        assertNull(ScoreUserCredentialRepository.joinTransports(Set.of()));
        assertTrue(ScoreUserCredentialRepository.splitTransports(null).isEmpty());
        assertTrue(ScoreUserCredentialRepository.splitTransports(" ").isEmpty());
    }

    @Test
    void anEmptyNameIsStoredAsNull() {
        assertNull(ScoreUserCredentialRepository.labelOrNull(""));
        assertNull(ScoreUserCredentialRepository.labelOrNull("  "));
        assertNull(ScoreUserCredentialRepository.labelOrNull(null));
        assertEquals("Work laptop", ScoreUserCredentialRepository.labelOrNull(" Work laptop "));
    }

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void userAgentOfTheCurrentRequestIsRecordedAndTrimmed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "x".repeat(600));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals(512, ScoreUserCredentialRepository.currentUserAgent().length());
    }

    @Test
    void noUserAgentWithoutARequestOrHeader() {
        assertNull(ScoreUserCredentialRepository.currentUserAgent());

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertNull(ScoreUserCredentialRepository.currentUserAgent());
    }

    private static CredentialRecord record(byte[] credentialId, byte[] userHandle) {
        return ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(new Bytes(credentialId))
                .userEntityUserId(new Bytes(userHandle))
                .publicKey(new ImmutablePublicKeyCose(new byte[]{1, 2, 3}))
                .signatureCount(0)
                .uvInitialized(true)
                .backupEligible(true)
                .backupState(false)
                .label("")
                .build();
    }

    private final PasskeyQueryRepository query = mock(PasskeyQueryRepository.class);
    private final PasskeyCommandRepository command = mock(PasskeyCommandRepository.class);
    private final PasskeyNotifier notifier = mock(PasskeyNotifier.class);

    private ScoreUserCredentialRepository repository() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.passkeyQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.passkeyCommandRepository(any())).thenReturn(command);
        ScoreUserCredentialRepository repository = new ScoreUserCredentialRepository();
        ReflectionTestUtils.setField(repository, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(repository, "sessionService", mock(SessionService.class));
        ReflectionTestUtils.setField(repository, "passkeyProperties",
                new PasskeyProperties("score.example.org", "", "https://score.example.org"));
        ReflectionTestUtils.setField(repository, "passkeyNotifier", notifier);
        return repository;
    }

    @Test
    void theOwnerIsToldWhenANewPasskeyIsStored() {
        UserId alice = new UserId(BigInteger.TEN);
        byte[] handle = {7, 7};
        when(query.findCredential(any())).thenReturn(Optional.empty());
        when(query.findPasskeyUserByHandle(handle))
                .thenReturn(Optional.of(new PasskeyUser(alice, "alice", "Alice", handle)));

        repository().save(record(new byte[]{1}, handle));

        verify(command).insertCredential(any());
        verify(notifier).passkeyAdded(alice);
    }

    @Test
    void nobodyIsToldWhenASignInRefreshesAPasskey() {
        when(query.findCredential(any())).thenReturn(Optional.of(mock(PasskeyCredential.class)));

        repository().save(record(new byte[]{1}, new byte[]{7, 7}));

        verify(command, never()).insertCredential(any());
        verify(notifier, never()).passkeyAdded(any());
    }

}
