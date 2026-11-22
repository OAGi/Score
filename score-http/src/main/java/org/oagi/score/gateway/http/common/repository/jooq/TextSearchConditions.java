package org.oagi.score.gateway.http.common.repository.jooq;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.oagi.score.gateway.http.common.model.TextSearchOperator;

/**
 * The SQL conditions of a text search over the text columns. The columns are compared in the database's collation (case
 * and accent insensitive); a case sensitive search compares them as binary strings.
 */
public final class TextSearchConditions {

    /** The escape character of LIKE patterns; it is not special in a string literal, unlike the backslash. */
    private static final char ESCAPE = '!';

    private TextSearchConditions() {
    }

    /**
     * Whether the database can hold the text. The text columns are utf8mb3: a character outside the basic plane cannot be
     * stored, and comparing a column with one is an error, so such a text is not sent to the database.
     */
    public static boolean isStorable(String value) {
        return value.codePoints().noneMatch(cp -> cp > 0xFFFF);
    }

    /**
     * The rows whose text satisfies the operator. A NULL text is an absent part: it is neither equal nor different.
     * The value has to be {@linkplain #isStorable storable}.
     */
    public static Condition matches(Field<String> text, TextSearchOperator operator, String value, boolean caseSensitive) {
        String pattern = switch (operator) {
            case EQUALS, NOT_EQUALS -> escape(value);
            case CONTAINS -> "%" + escape(value) + "%";
            case STARTS_WITH -> escape(value) + "%";
            case ENDS_WITH -> "%" + escape(value);
        };
        Condition like = caseSensitive
                ? DSL.condition("{0} like binary {1} escape '" + ESCAPE + "'", text, DSL.val(pattern))
                : text.like(pattern, ESCAPE);
        return operator == TextSearchOperator.NOT_EQUALS ? text.isNotNull().and(DSL.not(like)) : like;
    }

    private static String escape(String value) {
        return value.replace(String.valueOf(ESCAPE), ESCAPE + "" + ESCAPE)
                .replace("%", ESCAPE + "%")
                .replace("_", ESCAPE + "_");
    }
}
