package org.oagi.score.gateway.http.helper;

import java.util.Random;
import java.util.UUID;

public final class ScoreGuid {

    private static String GUID_PREFIX = "oagis-id-";

    private ScoreGuid() {
    }

    public static String randomGuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getGuidWithPrefix(String guid) {
        return guid.startsWith(GUID_PREFIX) ? guid : GUID_PREFIX + guid;
    }

    public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 6) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}
