package org.oagi.score.gateway.http.common.util;

public abstract class BooleanUtils {
    public static byte BooleanToByte(boolean value) {
        if (value) {
            return 1;
        }
        return 0;
    }
}
