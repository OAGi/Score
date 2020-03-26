package org.oagi.srt.gateway.http.helper.filter;

import org.jooq.tools.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AnyWordContainsFilter<T> implements Predicate<T> {

    private List<String> queries;
    private Function<T, String> mapper;
    private String splitRegex;
    private boolean ignoreCase;

    public AnyWordContainsFilter(String query, Function<T, String> mapper, boolean ignoreCase) {
        this(query, mapper, ignoreCase, " ");
    }

    public AnyWordContainsFilter(String query, Function<T, String> mapper, boolean ignoreCase, String splitRegex) {
        this.mapper = mapper;
        this.splitRegex = splitRegex;
        this.ignoreCase = ignoreCase;

        this.queries = this.split(query);
    }

    private List<String> split(String s) {
        if (StringUtils.isEmpty(s)) {
            return Collections.emptyList();
        }
        String q = ((ignoreCase) ? s.toLowerCase() : s);
        return ContainsFilterBuilder.split(q, splitRegex);
    }

    @Override
    public boolean test(T t) {
        if (this.queries.isEmpty()) {
            return true;
        }
        String s = mapper.apply(t);
        if (StringUtils.isEmpty(s)) {
            return false;
        }
        s = (ignoreCase) ? s.toLowerCase() : s;

        return this.queries.stream().allMatch(s::contains);
    }
}
