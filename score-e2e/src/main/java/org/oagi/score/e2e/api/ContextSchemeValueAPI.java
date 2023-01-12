package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.ContextSchemeObject;
import org.oagi.score.e2e.obj.ContextSchemeValueObject;

import java.math.BigInteger;

/**
 * APIs for the context scheme value management.
 */
public interface ContextSchemeValueAPI {

    /**
     * Return the context scheme value associated with the given context scheme value ID.
     *
     * @param contextSchemeValueId context scheme value ID
     * @return context scheme value object
     */
    ContextSchemeValueObject getContextSchemeValueById(BigInteger contextSchemeValueId);

    /**
     * Create a random context scheme value associated with the given context scheme.
     *
     * @param contextScheme context scheme
     * @return a created context scheme value
     */
    ContextSchemeValueObject createRandomContextSchemeValue(ContextSchemeObject contextScheme);

    void deleteContextSchemeValueById(BigInteger valueId);

}
