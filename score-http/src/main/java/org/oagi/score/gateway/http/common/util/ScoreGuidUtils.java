package org.oagi.score.gateway.http.common.util;

import java.util.UUID;

public abstract class ScoreGuidUtils {

    public static String randomGuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
