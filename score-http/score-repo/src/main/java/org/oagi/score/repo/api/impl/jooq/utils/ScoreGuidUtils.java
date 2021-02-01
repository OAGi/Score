package org.oagi.score.repo.api.impl.jooq.utils;

import java.util.UUID;

public abstract class ScoreGuidUtils {

    public static String randomGuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
