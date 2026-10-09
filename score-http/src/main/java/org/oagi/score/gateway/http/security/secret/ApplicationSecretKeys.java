package org.oagi.score.gateway.http.security.secret;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * Derives purpose-specific keys from the application secret key (the active key of the ring that
 * {@link ApplicationSecretCrypto} encrypts stored secrets with), for features that need a stable secret but do not
 * encrypt anything. A derived key never equals the application key, and a different purpose gives an unrelated key.
 */
@Component
public class ApplicationSecretKeys {

    private static final String DERIVATION_LABEL = "score:app-secret:derive:v1";

    private final SecretEncryptionProperties properties;

    /**
     * @param crypto not used directly; asking for it makes sure the key ring has been validated (a malformed key
     *               stops the application at startup) before anything is derived from it
     */
    @Autowired
    public ApplicationSecretKeys(SecretEncryptionProperties properties, ApplicationSecretCrypto crypto) {
        this.properties = properties;
    }

    /**
     * @return a 32-byte key for the purpose, or empty when no application secret key is configured
     */
    public Optional<byte[]> derive(String purpose) {
        String activeKeyId = properties.getActiveKeyId();
        String encoded = (activeKeyId == null) ? null : properties.getKeys().get(activeKeyId);
        if (encoded == null || encoded.isEmpty()) {
            return Optional.empty();
        }
        byte[] key = Base64.getDecoder().decode(encoded);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return Optional.of(mac.doFinal((DERIVATION_LABEL + "\u0000" + purpose).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not derive a key from the application secret key.", e);
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

}
