package org.oagi.score.gateway.http.api.application_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.application_management.model.PasskeySettingsInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Passkey sign-in has no on/off flag: it is enabled exactly when an RP ID and an allowed origin are saved.
 */
class PasskeySettingsValidatorTest {

    private static PasskeySettingsInfo settings(String rpId, String rpName, String... origins) {
        PasskeySettingsInfo info = new PasskeySettingsInfo();
        info.setRpId(rpId);
        info.setRpName(rpName);
        info.setAllowedOrigins(List.of(origins));
        return info;
    }

    @Test
    void emptySettingsAreValidAndMeanDisabled() {
        PasskeySettingsInfo normalized = PasskeySettingsValidator.validateAndNormalize(settings("", "", "  "));
        assertFalse(normalized.isEnabled());
        assertTrue(normalized.getAllowedOrigins().isEmpty());
    }

    @Test
    void nullFieldsMeanDisabled() {
        PasskeySettingsInfo info = new PasskeySettingsInfo();
        info.setAllowedOrigins(null);
        assertFalse(PasskeySettingsValidator.validateAndNormalize(info).isEnabled());
    }

    @Test
    void validSettingsAreEnabledAndNormalized() {
        PasskeySettingsInfo normalized = PasskeySettingsValidator.validateAndNormalize(
                settings("  Score.Example.ORG ", "  connectCenter ", "https://score.example.org/", "https://score.example.org"));
        assertTrue(normalized.isEnabled());
        assertEquals("score.example.org", normalized.getRpId());
        assertEquals("connectCenter", normalized.getRpName());
        assertEquals(List.of("https://score.example.org"), normalized.getAllowedOrigins());
    }

    @Test
    void rpNameDefaultsToTheRpId() {
        PasskeySettingsInfo normalized = PasskeySettingsValidator.validateAndNormalize(
                settings("score.example.org", " ", "https://score.example.org"));
        assertEquals(null, normalized.getRpName());
        assertEquals("score.example.org", normalized.effectiveRpName());
    }

    @Test
    void rpIdMayBeAParentDomainOfTheOrigins() {
        PasskeySettingsInfo normalized = PasskeySettingsValidator.validateAndNormalize(
                settings("example.org", null, "https://a.example.org", "https://b.example.org:8443"));
        assertTrue(normalized.isEnabled());
        assertEquals(2, normalized.getAllowedOrigins().size());
    }

    @Test
    void localhostMayUseHttpAndAPort() {
        assertTrue(PasskeySettingsValidator.validateAndNormalize(
                settings("localhost", null, "http://localhost:4200")).isEnabled());
    }

    @Test
    void partialSettingsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PasskeySettingsValidator.validateAndNormalize(
                settings("", "connectCenter", "https://score.example.org")));
        assertThrows(IllegalArgumentException.class, () -> PasskeySettingsValidator.validateAndNormalize(
                settings("score.example.org", null)));
    }

    @Test
    void rpIdMustBeABareDomain() {
        for (String bad : List.of("https://score.example.org", "score.example.org:8443", "score.example.org/path",
                "score example.org", "-bad.example.org")) {
            assertThrows(IllegalArgumentException.class, () -> PasskeySettingsValidator.validateAndNormalize(
                    settings(bad, null, "https://score.example.org")), bad);
        }
    }

    @Test
    void originMustBeUnderTheRpId() {
        assertThrows(IllegalArgumentException.class, () -> PasskeySettingsValidator.validateAndNormalize(
                settings("score.example.org", null, "https://other.example.org")));
        // a suffix match must be on a label boundary
        assertThrows(IllegalArgumentException.class, () -> PasskeySettingsValidator.validateAndNormalize(
                settings("example.org", null, "https://evilexample.org")));
    }

    @Test
    void originMustBeAnOriginOnly() {
        for (String bad : List.of("score.example.org", "https://score.example.org/login",
                "https://score.example.org?x=1", "https://user@score.example.org", "ftp://score.example.org")) {
            assertThrows(IllegalArgumentException.class, () -> PasskeySettingsValidator.validateAndNormalize(
                    settings("score.example.org", null, bad)), bad);
        }
    }

    @Test
    void plainHttpIsRejectedExceptForLocalhost() {
        assertThrows(IllegalArgumentException.class, () -> PasskeySettingsValidator.validateAndNormalize(
                settings("score.example.org", null, "http://score.example.org")));
    }

}
