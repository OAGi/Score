package org.oagi.score.gateway.http.common.util;

import org.jooq.Condition;
import org.jooq.Field;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DSLUtils {

    public static boolean isNull(BigInteger val) {
        return val == null || BigInteger.ONE.compareTo(val) == 1;
    }

    public static boolean isQuoted(String s) {
        if (!StringUtils.hasLength(s)) {
            return false;
        }
        if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return true;
        }
        return false;
    }

    public static String unquote(String s) {
        if (isQuoted(s)) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    public static List<String> split(String q) {
        return split(q, " ");
    }

    public static List<String> split(String q, String sep) {
        return Arrays.asList(q.split(sep)).stream()
                .collect(Collectors.toList());
    }

    public static Collection<Condition> contains(String s, Field<String> field) {
        if (s == null) s = "";
        String q = s.trim();
        if (isQuoted(q)) {
            return Arrays.asList(field.containsIgnoreCase(unquote(q)));
        } else {
            return split(q).stream()
                    .map(_s -> field.containsIgnoreCase(_s))
                    .collect(Collectors.toList());
        }
    }

}
