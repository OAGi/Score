package org.oagi.score.repo.api.impl.utils;

public abstract class BooleanUtils {
    public static byte BooleanToByte(boolean value) {
        if (value) {
            return 1;
        }
        return 0;
    }
}
