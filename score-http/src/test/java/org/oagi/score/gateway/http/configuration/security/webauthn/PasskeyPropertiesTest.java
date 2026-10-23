package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.application_management.model.PasskeySettingsInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Passkey sign-in is configured with {@code score.passkey.*} (application.yml or the {@code SCORE_PASSKEY_*}
 * environment variables). It has no on/off flag: it is on exactly when an RP ID and an allowed origin are set.
 */
class PasskeyPropertiesTest {

    @Test
    void nothingSetMeansDisabled() {
        PasskeyProperties properties = new PasskeyProperties("", "", "");

        assertFalse(properties.isEnabled());
        assertFalse(properties.current().isEnabled());
        assertTrue(properties.current().getAllowedOrigins().isEmpty());
    }

    @Test
    void unsetEnvironmentVariablesMeanDisabled() {
        assertFalse(new PasskeyProperties(null, null, null).isEnabled());
        assertFalse(new PasskeyProperties("  ", "  ", "  ").isEnabled());
    }

    @Test
    void anRpIdAndAnOriginEnableIt() {
        PasskeyProperties properties = new PasskeyProperties("score.example.org", "", "https://score.example.org");

        assertTrue(properties.isEnabled());
        assertEquals("score.example.org", properties.current().getRpId());
        assertEquals(List.of("https://score.example.org"), properties.current().getAllowedOrigins());
    }

    @Test
    void theRpNameIsOptionalAndDefaultsToTheRpId() {
        assertEquals("score.example.org",
                new PasskeyProperties("score.example.org", "", "https://score.example.org").current().effectiveRpName());
        assertEquals("connectCenter",
                new PasskeyProperties("score.example.org", "connectCenter", "https://score.example.org").current().effectiveRpName());
    }

    @Test
    void originsMayBeSeparatedByCommasOrWhitespace() {
        PasskeyProperties properties = new PasskeyProperties("example.org", "",
                "https://a.example.org, https://b.example.org\nhttps://c.example.org:8443  ,");

        assertEquals(List.of("https://a.example.org", "https://b.example.org", "https://c.example.org:8443"),
                properties.current().getAllowedOrigins());
    }

    @Test
    void valuesAreNormalized() {
        PasskeyProperties properties = new PasskeyProperties(" Score.Example.ORG ", " ", "https://score.example.org/");

        assertEquals("score.example.org", properties.current().getRpId());
        assertEquals(List.of("https://score.example.org"), properties.current().getAllowedOrigins());
    }

    @Test
    void localhostMayUsePlainHttp() {
        assertTrue(new PasskeyProperties("localhost", "", "http://localhost:4200").isEnabled());
    }

    @Test
    void aMistakeStopsTheApplicationWithTheReason() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new PasskeyProperties("example.org", "", "https://other.org"));
        assertTrue(ex.getMessage().contains("score.passkey"), ex.getMessage());
        assertTrue(ex.getMessage().contains("https://other.org"), ex.getMessage());

        assertThrows(IllegalStateException.class, () -> new PasskeyProperties("score.example.org", "", ""));
        assertThrows(IllegalStateException.class, () -> new PasskeyProperties("", "", "https://score.example.org"));
        assertThrows(IllegalStateException.class,
                () -> new PasskeyProperties("https://score.example.org", "", "https://score.example.org"));
    }

    @Test
    void callersCannotChangeTheConfiguration() {
        PasskeyProperties properties = new PasskeyProperties("score.example.org", "", "https://score.example.org");

        PasskeySettingsInfo copy = properties.current();
        copy.setRpId("evil.example.org");
        copy.getAllowedOrigins().add("https://evil.example.org");

        assertEquals("score.example.org", properties.current().getRpId());
        assertEquals(List.of("https://score.example.org"), properties.current().getAllowedOrigins());
    }

}
