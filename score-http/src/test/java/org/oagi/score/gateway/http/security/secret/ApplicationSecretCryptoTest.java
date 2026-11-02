package org.oagi.score.gateway.http.security.secret;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationSecretCryptoTest {

    private static SecretEncryptionProperties properties(String active, Map<String, String> keys) {
        SecretEncryptionProperties properties = new SecretEncryptionProperties();
        properties.setActiveKeyId(active);
        properties.setKeys(keys);
        return properties;
    }

    private static String key(char value) {
        return Base64.getEncoder().encodeToString(
                String.valueOf(value).repeat(32).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void encryptsWithUniqueNoncesAndAuthenticatesContext() {
        ApplicationSecretCrypto crypto = new ApplicationSecretCrypto(
                properties("v1", Map.of("v1", key('a'))));
        var context = new ApplicationSecretCrypto.SecretContext(
                "guid", "ai-provider/1", "AI_PROVIDER_API_KEY");

        var first = crypto.encrypt("secret".toCharArray(), context);
        var second = crypto.encrypt("secret".toCharArray(), context);

        assertThat(first.nonce()).isNotEqualTo(second.nonce());
        assertThat(first.ciphertext()).isNotEqualTo(second.ciphertext());
        assertThat(crypto.decrypt(first, context)).containsExactly("secret".toCharArray());
        assertThatThrownBy(() -> crypto.decrypt(first,
                new ApplicationSecretCrypto.SecretContext(
                        "other", "ai-provider/1", "AI_PROVIDER_API_KEY")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decryptsOldAndNewKeysDuringRotationAndRejectsTampering() {
        var context = new ApplicationSecretCrypto.SecretContext(
                "guid", "ai-provider/1", "AI_PROVIDER_API_KEY");
        ApplicationSecretCrypto oldCrypto = new ApplicationSecretCrypto(
                properties("old", Map.of("old", key('a'))));
        var oldEnvelope = oldCrypto.encrypt("old-secret".toCharArray(), context);
        ApplicationSecretCrypto rotating = new ApplicationSecretCrypto(properties("new",
                Map.of("old", key('a'), "new", key('b'))));
        var newEnvelope = rotating.encrypt("new-secret".toCharArray(), context);

        assertThat(rotating.decrypt(oldEnvelope, context)).containsExactly("old-secret".toCharArray());
        assertThat(rotating.decrypt(newEnvelope, context)).containsExactly("new-secret".toCharArray());
        byte[] tampered = oldEnvelope.ciphertext();
        tampered[0] ^= 1;
        var corrupt = new ApplicationSecretCrypto.EncryptedSecret(tampered,
                oldEnvelope.nonce(), oldEnvelope.keyId(), oldEnvelope.version());
        assertThatThrownBy(() -> rotating.decrypt(corrupt, context))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsInvalidKeyMaterial() {
        ApplicationSecretCrypto crypto = new ApplicationSecretCrypto(
                properties("v1", Map.of("v1", Base64.getEncoder()
                        .encodeToString(new byte[16]))));
        assertThatThrownBy(() -> crypto.encrypt("secret".toCharArray(),
                new ApplicationSecretCrypto.SecretContext(
                        "guid", "name", "AI_PROVIDER_API_KEY")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void permitsMissingKeyMaterialAtStartupAndRejectsItWhenEncryptionIsUsed() {
        ApplicationSecretCrypto missing = new ApplicationSecretCrypto(
                properties("primary", Map.of("primary", "")));
        missing.validateConfiguration();
        assertThat(missing.isConfigured()).isFalse();
        assertThatThrownBy(() -> missing.encrypt("secret".toCharArray(),
                new ApplicationSecretCrypto.SecretContext(
                        "guid", "name", "AI_PROVIDER_API_KEY")))
                .isInstanceOf(ApplicationSecretUnavailableException.class)
                .hasMessageContaining("not configured")
                .hasMessageContaining("before using encrypted provider credentials");

    }

    @Test
    void rejectsMalformedActiveAndInactiveKeysDuringStartupValidation() {
        assertThatThrownBy(() -> new ApplicationSecretCrypto(
                properties("primary", Map.of("primary", "not-base64"))).validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
        assertThatThrownBy(() -> new ApplicationSecretCrypto(
                properties("primary", Map.of("primary", "   "))).validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
        assertThatThrownBy(() -> new ApplicationSecretCrypto(properties("active",
                Map.of("active", key('a'), "retired", "not-base64")))
                .validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
    }

    @Test
    void rejectsAConfiguredKeyWithTheWrongDecodedLengthDuringStartupValidation() {
        assertThatThrownBy(() -> new ApplicationSecretCrypto(properties("primary",
                Map.of("primary", Base64.getEncoder().encodeToString(new byte[16]))))
                .validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }
}
