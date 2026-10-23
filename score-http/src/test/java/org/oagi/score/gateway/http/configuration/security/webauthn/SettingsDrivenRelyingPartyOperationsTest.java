package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.application_management.model.PasskeySettingsInfo;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialParameters;
import org.springframework.security.web.webauthn.api.ResidentKeyRequirement;
import org.springframework.security.web.webauthn.api.UserVerificationRequirement;
import org.springframework.security.web.webauthn.management.ImmutablePublicKeyCredentialRequestOptionsRequest;
import org.springframework.security.web.webauthn.management.MapPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.MapUserCredentialRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SettingsDrivenRelyingPartyOperationsTest {

    private static PasskeySettingsInfo settings(String rpId, String... origins) {
        PasskeySettingsInfo info = new PasskeySettingsInfo();
        info.setRpId(rpId);
        info.setAllowedOrigins(List.of(origins));
        return info;
    }

    private static SettingsDrivenRelyingPartyOperations operations(AtomicReference<PasskeySettingsInfo> current) {
        return new SettingsDrivenRelyingPartyOperations(
                new MapPublicKeyCredentialUserEntityRepository(), new MapUserCredentialRepository(), current::get);
    }

    private static PublicKeyCredentialRequestOptions requestOptions(SettingsDrivenRelyingPartyOperations operations) {
        return operations.createCredentialRequestOptions(new ImmutablePublicKeyCredentialRequestOptionsRequest(null));
    }

    @Test
    void notConfiguredFailsClearlyButDoesNotFailAtConstruction() {
        AtomicReference<PasskeySettingsInfo> current = new AtomicReference<>(new PasskeySettingsInfo());
        SettingsDrivenRelyingPartyOperations operations = operations(current);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> requestOptions(operations));
        assertEquals("Passkey sign-in is not configured.", ex.getMessage());
    }

    @Test
    void usesTheSavedRpIdAndRequiresUserVerification() {
        AtomicReference<PasskeySettingsInfo> current =
                new AtomicReference<>(settings("score.example.org", "https://score.example.org"));
        PublicKeyCredentialRequestOptions options = requestOptions(operations(current));

        assertEquals("score.example.org", options.getRpId());
        assertEquals(UserVerificationRequirement.REQUIRED, options.getUserVerification());
    }

    @Test
    void registrationRequiresAResidentKeyAndUserVerificationAndListsEs256First() {
        // Spring's default asks for user verification only as "preferred", which lets an assertion without the
        // user-verified flag through; both registration and sign-in must require it.
        AtomicReference<PasskeySettingsInfo> current =
                new AtomicReference<>(settings("score.example.org", "https://score.example.org"));
        Authentication alice = new TestingAuthenticationToken("alice", "x", "ROLE_END_USER");

        PublicKeyCredentialCreationOptions options =
                operations(current).createPublicKeyCredentialCreationOptions(() -> alice);

        assertEquals(UserVerificationRequirement.REQUIRED, options.getAuthenticatorSelection().getUserVerification());
        assertEquals(ResidentKeyRequirement.REQUIRED, options.getAuthenticatorSelection().getResidentKey());
        assertEquals(PublicKeyCredentialParameters.ES256, options.getPubKeyCredParams().get(0));
        assertEquals("score.example.org", options.getRp().getId());
    }

    @Test
    void picksUpChangedSettingsWithoutARestart() {
        AtomicReference<PasskeySettingsInfo> current =
                new AtomicReference<>(settings("old.example.org", "https://old.example.org"));
        SettingsDrivenRelyingPartyOperations operations = operations(current);
        assertEquals("old.example.org", requestOptions(operations).getRpId());

        current.set(settings("new.example.org", "https://new.example.org"));
        assertEquals("new.example.org", requestOptions(operations).getRpId());

        current.set(new PasskeySettingsInfo());
        assertThrows(IllegalStateException.class, () -> requestOptions(operations));
    }

}
