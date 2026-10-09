package org.oagi.score.gateway.http.api.passkey_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyCredential;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeySummary;

import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasskeyQueryServiceTest {

    private static PasskeyCredential credential(String rpId, String transports) {
        LocalDateTime now = LocalDateTime.of(2026, 10, 8, 9, 0);
        return new PasskeyCredential(new byte[]{(byte) 0xfb, (byte) 0xff, 0x01}, new byte[]{1}, new UserId(BigInteger.ONE),
                rpId, new byte[]{2}, 0, true, true, false, transports, "public-key", null, null,
                "MacBook Touch ID", now, now, null, null);
    }

    @Test
    void idIsUnpaddedBase64Url() {
        PasskeySummary summary = PasskeyQueryService.toSummary(credential("score.example.org", null), "score.example.org");
        assertEquals("-_8B", summary.id());
    }

    @Test
    void userHandleIsUnpaddedBase64Url() {
        assertEquals("AQ", PasskeyQueryService.toSummary(credential("a", null), "a").userHandle());
    }

    @Test
    void passkeyIsUsableOnlyForTheConfiguredRpId() {
        assertTrue(PasskeyQueryService.toSummary(credential("score.example.org", null), "score.example.org").usable());
        assertTrue(PasskeyQueryService.toSummary(credential("Score.Example.org", null), "score.example.org").usable());
        assertFalse(PasskeyQueryService.toSummary(credential("old.example.org", null), "score.example.org").usable());
        assertFalse(PasskeyQueryService.toSummary(credential("score.example.org", null), "").usable());
        assertFalse(PasskeyQueryService.toSummary(credential("score.example.org", null), null).usable());
    }

    @Test
    void transportsAreSplit() {
        assertEquals(java.util.List.of("internal", "hybrid"),
                PasskeyQueryService.toSummary(credential("a", "internal, hybrid"), "a").transports());
        assertTrue(PasskeyQueryService.toSummary(credential("a", null), "a").transports().isEmpty());
    }


    private PasskeyQueryService lookupService(String rpId, PasskeyQueryRepository repository) {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.passkeyQueryRepository(any())).thenReturn(repository);
        PasskeyQueryService service = new PasskeyQueryService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "passkeyProperties", new PasskeyProperties(rpId, "",
                rpId.isEmpty() ? "" : "https://" + rpId));
        return service;
    }

    @Test
    void lookupFindsAUsablePasskey() {
        PasskeyQueryRepository repository = mock(PasskeyQueryRepository.class);
        UserId alice = new UserId(BigInteger.ONE);
        when(repository.findUserIdByLoginId("alice")).thenReturn(Optional.of(alice));
        when(repository.findCredentialsByUserId(alice)).thenReturn(List.of(credential("score.example.org", null)));

        assertTrue(lookupService("score.example.org", repository).hasUsablePasskey(null, " alice "));
    }

    @Test
    void lookupIgnoresPasskeysOfAnotherRpId() {
        PasskeyQueryRepository repository = mock(PasskeyQueryRepository.class);
        UserId alice = new UserId(BigInteger.ONE);
        when(repository.findUserIdByLoginId("alice")).thenReturn(Optional.of(alice));
        when(repository.findCredentialsByUserId(alice)).thenReturn(List.of(credential("old.example.org", null)));

        assertFalse(lookupService("score.example.org", repository).hasUsablePasskey(null, "alice"));
    }

    @Test
    void lookupAnswersFalseForUnknownAccountsBlankNamesAndWhenDisabled() {
        PasskeyQueryRepository repository = mock(PasskeyQueryRepository.class);
        when(repository.findUserIdByLoginId(any())).thenReturn(Optional.empty());

        PasskeyQueryService enabled = lookupService("score.example.org", repository);
        assertFalse(enabled.hasUsablePasskey(null, "nobody"));
        assertFalse(enabled.hasUsablePasskey(null, "  "));
        assertFalse(enabled.hasUsablePasskey(null, null));
        assertFalse(lookupService("", repository).hasUsablePasskey(null, "alice"));
    }
}
