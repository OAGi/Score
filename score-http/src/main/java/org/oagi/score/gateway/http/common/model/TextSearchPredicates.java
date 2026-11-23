package org.oagi.score.gateway.http.common.model;

import java.util.List;

/** What a request of server-side search predicates may ask: more predicates or longer text than a person types into a search bar is a mistake, or an attack. */
public final class TextSearchPredicates {

    public static final int MAX_PREDICATES = 20;
    public static final int MAX_VALUE_LENGTH = 1000;

    private TextSearchPredicates() {
    }

    /** @throws IllegalArgumentException if there are too many predicates, or one lacks what it needs or has too long a text */
    public static void validate(List<? extends TextSearchPredicate> predicates) {
        if (predicates == null || predicates.size() > MAX_PREDICATES) {
            throw new IllegalArgumentException("A search has at most " + MAX_PREDICATES + " server predicates.");
        }
        for (TextSearchPredicate predicate : predicates) {
            if (predicate.id() == null || predicate.id().isEmpty() || predicate.operator() == null
                    || predicate.value() == null || predicate.value().length() > MAX_VALUE_LENGTH) {
                throw new IllegalArgumentException("A server predicate needs an id, an operator and a value of at most "
                        + MAX_VALUE_LENGTH + " characters.");
            }
        }
    }
}
