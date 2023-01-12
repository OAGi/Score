package org.oagi.score.e2e.obj;

import lombok.SneakyThrows;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ObjectHelper {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private ObjectHelper() {
    }

    @SneakyThrows(NoSuchAlgorithmException.class)
    public static String sha256(String str) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return new String(hexEncode(digest.digest(str.getBytes())));
    }

    public static char[] hexEncode(byte[] bytes) {
        final int nBytes = bytes.length;
        char[] result = new char[2 * nBytes];
        int j = 0;
        for (byte aByte : bytes) {
            // Char for top 4 bits
            result[j++] = HEX[(0xF0 & aByte) >>> 4];
            // Bottom 4
            result[j++] = HEX[(0x0F & aByte)];
        }
        return result;
    }

}
