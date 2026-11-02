package org.oagi.score.gateway.http.security.secret;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationSecretKeysTest {

    private static final byte[] RAW = new byte[32];

    static {
        for (int i = 0; i < RAW.length; i++) {
            RAW[i] = (byte) (i + 1);
        }
    }

    private static ApplicationSecretKeys keys(String activeKeyId, Map<String, String> ring) {
        SecretEncryptionProperties properties = new SecretEncryptionProperties();
        properties.setActiveKeyId(activeKeyId);
        properties.setKeys(ring);
        return new ApplicationSecretKeys(properties, new ApplicationSecretCrypto(properties));
    }

    private static ApplicationSecretKeys keys(byte[] raw) {
        return keys("primary", Map.of("primary", Base64.getEncoder().encodeToString(raw)));
    }

    @Test
    void aDerivedKeyIsStableForTheSameSecretAndPurpose() {
        byte[] first = keys(RAW).derive("passkey-lookup-decoy").orElseThrow();
        assertEquals(32, first.length);
        assertArrayEquals(first, keys(RAW).derive("passkey-lookup-decoy").orElseThrow());
    }

    @Test
    void anotherPurposeOrAnotherSecretGivesAnUnrelatedKey() {
        byte[] key = keys(RAW).derive("passkey-lookup-decoy").orElseThrow();
        assertFalse(Arrays.equals(key, keys(RAW).derive("something-else").orElseThrow()));
        byte[] other = RAW.clone();
        other[0] ^= 1;
        assertFalse(Arrays.equals(key, keys(other).derive("passkey-lookup-decoy").orElseThrow()));
    }

    @Test
    void theApplicationKeyItselfIsNeverTheDerivedKey() {
        assertFalse(Arrays.equals(RAW, keys(RAW).derive("passkey-lookup-decoy").orElseThrow()));
    }

    @Test
    void onlyTheActiveKeyOfTheRingIsUsed() {
        String old = Base64.getEncoder().encodeToString(new byte[32]);
        String current = Base64.getEncoder().encodeToString(RAW);
        byte[] viaRing = keys("current", Map.of("old", old, "current", current)).derive("p").orElseThrow();
        assertArrayEquals(keys(RAW).derive("p").orElseThrow(), viaRing);
    }

    @Test
    void withoutAKeyThereIsNothingToDerive() {
        assertTrue(keys("primary", Map.of("primary", "")).derive("p").isEmpty());
        assertTrue(keys("primary", Map.of()).derive("p").isEmpty());
    }

}
