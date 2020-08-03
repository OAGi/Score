package org.oagi.score.gateway.http.helper.filter;

import org.jooq.tools.StringUtils;

import java.util.function.Function;
import java.util.function.Predicate;

public class ExactMatchContainsFilter<T> implements Predicate<T> {

    private String query;
    private Function<T, String> mapper;
    private boolean ignoreCase;

    public ExactMatchContainsFilter(String query, Function<T, String> mapper, boolean ignoreCase) {
        this.query = (StringUtils.isEmpty(query)) ? null : ((ignoreCase) ? query.toLowerCase() : query);
        this.mapper = mapper;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean test(T t) {
        if (this.query == null) {
            return true;
        }
        String s = mapper.apply(t);
        if (StringUtils.isEmpty(s)) {
            return false;
        }

        if (this.ignoreCase) {
            if (s.toLowerCase().contains(query)) {
                return true;
            }
        } else {
            if (s.contains(query)) {
                return true;
            }
        }

        return false;
    }

}
