package org.oagi.score.gateway.http.helper.filter;

import org.apache.commons.lang3.stream.Streams;
import org.jooq.Condition;
import org.jooq.Field;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.or;

public class ContainsFilterBuilder {

    public static <T> Predicate<T> contains(String query, Function<T, String> mapper) {
        return contains(query, mapper, true);
    }

    public static <T> Predicate<T> contains(String query, Function<T, String> mapper, boolean ignoreCase) {
        if (!StringUtils.hasLength(query)) {
            return e -> true;
        }
        query = query.trim();
        if (isQuoted(query)) {
            return new ExactMatchContainsFilter<T>(unquote(query), mapper, ignoreCase);
        }
        return new AnyWordContainsFilter<T>(query, mapper, ignoreCase);
    }

    public static boolean isQuoted(String s) {
        if (!StringUtils.hasLength(s)) {
            return false;
        }
        return s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"';
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
        String q = s.trim();
        if (isQuoted(q)) {
            return Arrays.asList(field.containsIgnoreCase(unquote(q)));
        } else {
            return split(q).stream()
                    .map(_s -> field.containsIgnoreCase(_s))
                    .collect(Collectors.toList());
        }
    }

    public static Collection<Condition> contains(String s, Field<String>... fields) {
        String q = s.trim();
        if (isQuoted(q)) {
            return Arrays.asList(
                    or(Streams.of(fields).map(f -> f.containsIgnoreCase(unquote(q))).collect(Collectors.toList()))
            );
        } else {
            return split(q).stream()
                    .map(_s ->
                            or(Streams.of(fields).map(f -> f.containsIgnoreCase(unquote(_s))).collect(Collectors.toList()))
                    )
                    .collect(Collectors.toList());
        }
    }

}
