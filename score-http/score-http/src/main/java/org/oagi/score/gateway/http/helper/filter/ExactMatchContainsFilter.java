package org.oagi.score.gateway.http.helper.filter;


import org.oagi.score.repo.api.impl.utils.StringUtils;

import java.util.function.Function;
import java.util.function.Predicate;

public class ExactMatchContainsFilter<T> implements Predicate<T> {

    private final String query;
    private final Function<T, String> mapper;
    private final boolean ignoreCase;

    public ExactMatchContainsFilter(String query, Function<T, String> mapper, boolean ignoreCase) {
        this.query = (!StringUtils.hasLength(query)) ? null : ((ignoreCase) ? query.toLowerCase() : query);
        this.mapper = mapper;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean test(T t) {
        if (this.query == null) {
            return true;
        }
        String s = mapper.apply(t);
        if (!StringUtils.hasLength(s)) {
            return false;
        }

        if (this.ignoreCase) {
            return s.toLowerCase().contains(query);
        } else {
            return s.contains(query);
        }
    }

}
