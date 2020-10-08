package org.oagi.score.gateway.http.helper;

import java.util.UUID;

public final class ScoreGuid {

    private ScoreGuid() {
    }

    public static String randomGuid() {
        return "oagis-id-" + (UUID.randomUUID().toString().replace("-", ""));
    }

}
