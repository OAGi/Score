package org.oagi.score.gateway.http.security.secret;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import jakarta.annotation.PostConstruct;

/** AES-256-GCM envelope encryption for application-managed provider credentials. */
@Component
public class ApplicationSecretCrypto {

    static final int VERSION = 1;
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretEncryptionProperties properties;
    private final SecureRandom random;

    @Autowired
    public ApplicationSecretCrypto(SecretEncryptionProperties properties) {
        this(properties, new SecureRandom());
    }

    ApplicationSecretCrypto(SecretEncryptionProperties properties, SecureRandom random) {
        this.properties = properties;
        this.random = random;
    }

    /** Validates a configured key ring; an absent ring leaves encrypted-secret features disabled. */
    @PostConstruct
    void validateConfiguration() {
        if (!isConfigured()) return;
        key(properties.getActiveKeyId());
        properties.getKeys().keySet().forEach(this::key);
    }

    boolean isConfigured() {
        return properties.getKeys().values().stream()
                .anyMatch(value -> value != null && !value.isEmpty());
    }

    public EncryptedSecret encrypt(char[] plaintext, SecretContext context) {
        if (plaintext == null || plaintext.length == 0) {
            throw new IllegalArgumentException("A secret value is required.");
        }
        String keyId = properties.getActiveKeyId();
        SecretKeySpec key = key(keyId);
        byte[] nonce = new byte[NONCE_BYTES];
        random.nextBytes(nonce);
        ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(plaintext));
        byte[] value = new byte[encoded.remaining()];
        encoded.get(value);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
            cipher.updateAAD(context.aad(VERSION));
            return new EncryptedSecret(cipher.doFinal(value), nonce, keyId, VERSION);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not encrypt the application secret.", exception);
        } finally {
            Arrays.fill(value, (byte) 0);
        }
    }

    public char[] decrypt(EncryptedSecret encrypted, SecretContext context) {
        if (encrypted == null || encrypted.version() != VERSION
                || encrypted.nonce() == null || encrypted.nonce().length != NONCE_BYTES) {
            throw new IllegalStateException("The application secret envelope is invalid.");
        }
        byte[] plaintext = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(encrypted.keyId()),
                    new GCMParameterSpec(TAG_BITS, encrypted.nonce()));
            cipher.updateAAD(context.aad(encrypted.version()));
            plaintext = cipher.doFinal(encrypted.ciphertext());
            CharBuffer decoded = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(plaintext));
            char[] value = new char[decoded.remaining()];
            decoded.get(value);
            return value;
        } catch (AEADBadTagException exception) {
            throw new IllegalStateException("The application secret could not be authenticated.");
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("The application secret could not be decrypted.");
        } finally {
            if (plaintext != null) Arrays.fill(plaintext, (byte) 0);
        }
    }

    private SecretKeySpec key(String keyId) {
        if (!StringUtils.hasText(keyId)) {
            throw new IllegalStateException("An active application secret encryption key ID is required.");
        }
        String encoded = properties.getKeys().get(keyId);
        if (encoded == null || encoded.isEmpty()) {
            throw new ApplicationSecretUnavailableException(
                    "Application secret encryption key '" + keyId
                    + "' is not configured. Configure the application secret key before "
                    + "using encrypted provider credentials.");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Application secret encryption keys must be Base64 encoded.");
        }
        if (decoded.length != 32) {
            Arrays.fill(decoded, (byte) 0);
            throw new IllegalStateException("Application secret encryption keys must contain 32 bytes.");
        }
        try {
            return new SecretKeySpec(decoded, "AES");
        } finally {
            Arrays.fill(decoded, (byte) 0);
        }
    }

    public record SecretContext(String guid, String name, String type) {
        public SecretContext {
            if (!StringUtils.hasText(guid) || !StringUtils.hasText(name)
                    || !StringUtils.hasText(type)) {
                throw new IllegalArgumentException("Secret GUID, name, and type are required.");
            }
        }

        byte[] aad(int version) {
            return (version + "\u0000" + guid + "\u0000" + name + "\u0000" + type)
                    .getBytes(StandardCharsets.UTF_8);
        }
    }

    public record EncryptedSecret(byte[] ciphertext, byte[] nonce, String keyId, int version) {
        public EncryptedSecret {
            ciphertext = ciphertext != null ? ciphertext.clone() : null;
            nonce = nonce != null ? nonce.clone() : null;
        }

        @Override public byte[] ciphertext() { return ciphertext.clone(); }
        @Override public byte[] nonce() { return nonce.clone(); }
    }
}
