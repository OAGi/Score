package org.oagi.srt.gateway.http.helper;

import java.util.UUID;

public final class SrtGuid {

    private SrtGuid() {
    }

    public static String randomGuid() {
        return "oagis-id-" + (UUID.randomUUID().toString().replace("-", ""));
    }

}
