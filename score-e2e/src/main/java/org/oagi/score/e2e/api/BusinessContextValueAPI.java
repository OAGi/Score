package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.BusinessContextValueObject;
import org.oagi.score.e2e.obj.ContextSchemeValueObject;

import java.math.BigInteger;

/**
 * APIs for the business context value management.
 */
public interface BusinessContextValueAPI {

    /**
     * Create a random business context value associated with the given business context and the context scheme value.
     *
     * @param businessContext    business context
     * @param contextSchemeValue context scheme value
     * @return a created business context value
     */
    BusinessContextValueObject createRandomBusinessContextValue(BusinessContextObject businessContext,
                                                                ContextSchemeValueObject contextSchemeValue);

    void deleteBusinessContextValueById(BigInteger businessContextValueId);

}
