package org.oagi.score.repo.api.impl.jooq.log;

import java.security.SecureRandom;

public abstract class LogUtils {

    private static class Holder {
        static final SecureRandom numberGenerator = new SecureRandom();
    }

    private static final char[] HEX_CHARS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static char[] encodeHex(byte[] bytes) {
        char[] chars = new char[32];
        for (int i = 0; i < chars.length; i = i + 2) {
            byte b = bytes[i / 2];
            chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
            chars[i + 1] = HEX_CHARS[b & 0xf];
        }
        return chars;
    }

    public static String generateHash() {
        SecureRandom ng = Holder.numberGenerator;

        byte[] randomBytes = new byte[20];
        ng.nextBytes(randomBytes);
        return new String(encodeHex(randomBytes));
    }
}
