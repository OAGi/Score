package org.oagi.score.e2e.api;

import org.jooq.types.ULong;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ContextCategoryObject;

import java.math.BigInteger;

/**
 * APIs for the context category management.
 */
public interface ContextCategoryAPI {

    /**
     * Return the context category associated with the given context category ID.
     *
     * @param contextCategoryId context category ID
     * @return context category object
     */
    ContextCategoryObject getContextCategoryById(BigInteger contextCategoryId);

    /**
     * Return the context category associated with the given context category name.
     *
     * @param contextCategoryName context category name
     * @return context category object
     */
    ContextCategoryObject getContextCategoryByName(String contextCategoryName);

    /**
     * Create the context category as requested.
     *
     * @param contextCategory context category object
     * @param creator         account who creates this context category
     * @return a created context category object
     */
    ContextCategoryObject createContextCategory(ContextCategoryObject contextCategory,
                                                AppUserObject creator);

    /**
     * Create a random context category.
     *
     * @param creator account who creates this context category
     * @return a created context category object
     */
    ContextCategoryObject createRandomContextCategory(AppUserObject creator);

    /**
     * Create a random context category.
     *
     * @param creator    account who creates this context category
     * @param namePrefix the prefix of the context category
     * @return a created context category object
     */
    ContextCategoryObject createRandomContextCategory(AppUserObject creator, String namePrefix);

    /**
     * Delete the context category by the given context category name.
     *
     * @param categoryName context category name
     */
    void deleteContextCategoryByName(String categoryName);

    void deleteContextCategoryById(ULong categoryId);

}
