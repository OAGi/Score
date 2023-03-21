package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.obj.ContextSchemeObject;

import java.math.BigInteger;

/**
 * APIs for the context scheme management.
 */
public interface ContextSchemeAPI {

    /**
     * Return the context scheme associated with the given context scheme ID.
     *
     * @param contextSchemeId context scheme ID
     * @return context scheme object
     */
    ContextSchemeObject getContextSchemeById(BigInteger contextSchemeId);

    /**
     * Return the context scheme associated with the given context scheme name.
     *
     * @param contextSchemeName context scheme name
     * @return context scheme object
     */
    ContextSchemeObject getContextSchemeByName(String contextSchemeName);

    /**
     * Create a random context scheme.
     *
     * @param contextCategory associated context category
     * @param creator         account who creates this context scheme
     * @return a created context scheme object
     */
    ContextSchemeObject createRandomContextScheme(ContextCategoryObject contextCategory,
                                                  AppUserObject creator);

    /**
     * Create a random context scheme.
     *
     * @param contextCategory associated context category
     * @param creator         account who creates this context scheme
     * @param namePrefix      the prefix of the context scheme name
     * @return a created context scheme object
     */
    ContextSchemeObject createRandomContextScheme(ContextCategoryObject contextCategory,
                                                  AppUserObject creator, String namePrefix);

    void deleteContextSchemaById(BigInteger contextSchemeId);

}
