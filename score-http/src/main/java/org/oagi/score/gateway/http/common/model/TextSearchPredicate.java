package org.oagi.score.gateway.http.common.model;

/**
 * One condition of a tree search that the server evaluates against stored text. The trees differ in the attributes they
 * can ask about, not in how a condition is made, so every kind of predicate has this shape.
 */
public interface TextSearchPredicate {

    /** What the client calls the predicate; the answer is given under it. */
    String id();

    /** How {@link #value()} is compared with the stored text. */
    TextSearchOperator operator();

    /** The text to look for. */
    String value();

    /** Whether upper and lower case letters differ. */
    boolean caseSensitive();
}
