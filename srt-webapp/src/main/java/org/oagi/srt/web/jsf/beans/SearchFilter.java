package org.oagi.srt.web.jsf.beans;

import org.apache.lucene.store.Directory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.oagi.srt.common.util.Utility.suggestWord;

public class SearchFilter<T> implements Predicate<T> {

    private String q;

    private Function<T, String> func;
    private String delim;

    public SearchFilter(String q, Directory directory, String field,
                        String delim, Function<T, String> func) {
        if (!StringUtils.isEmpty(q)) {
            this.q = Arrays.asList(q.split(Pattern.quote(delim))).stream()
                    .map(s -> s.replaceAll("[.]", ""))
                    .map(s -> suggestWord(s.toLowerCase(), directory, field))
                    .collect(Collectors.joining(delim));
        } else {
            this.q = q;
        }

        this.func = func;
        this.delim = delim;
    }

    @Override
    public boolean test(T t) {
        if (StringUtils.isEmpty(q)) {
            return true;
        }

        String str = this.func.apply(t);
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        List<String> list = Arrays.asList(str.toLowerCase().split(Pattern.quote(delim))).stream()
                .map(s -> s.replaceAll("[.]", ""))
                .collect(Collectors.toList());
        String[] split = q.split(Pattern.quote(delim));

        for (String s : split) {
            if (!list.contains(s)) {
                return false;
            }
        }
        return true;
    }


}
