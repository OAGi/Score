package org.oagi.score.gateway.http.helper;

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

}
