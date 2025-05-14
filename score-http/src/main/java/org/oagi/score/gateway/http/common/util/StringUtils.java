package org.oagi.score.gateway.http.common.util;

public abstract class StringUtils {

    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    public static String trim(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
    }

    public static boolean equals(String o1, String o2) {
        return o1 == null ? o2 == null : trim(o1).equals(trim(o2));
    }

}
