/*
 * This file is generated by jOOQ.
 */
package org.oagi.score.repo.api.impl.jooq.entity;


import org.jooq.Configuration;
import org.jooq.Field;
import org.oagi.score.repo.api.impl.jooq.entity.routines.Levenshtein;


/**
 * Convenience access to all stored procedures and functions in oagi.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Routines {

    /**
     * Call <code>oagi.levenshtein</code>
     */
    public static Integer levenshtein(
          Configuration configuration
        , String s1
        , String s2
    ) {
        Levenshtein f = new Levenshtein();
        f.setS1(s1);
        f.setS2(s2);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>oagi.levenshtein</code> as a field.
     */
    public static Field<Integer> levenshtein(
          String s1
        , String s2
    ) {
        Levenshtein f = new Levenshtein();
        f.setS1(s1);
        f.setS2(s2);

        return f.asField();
    }

    /**
     * Get <code>oagi.levenshtein</code> as a field.
     */
    public static Field<Integer> levenshtein(
          Field<String> s1
        , Field<String> s2
    ) {
        Levenshtein f = new Levenshtein();
        f.setS1(s1);
        f.setS2(s2);

        return f.asField();
    }
}
