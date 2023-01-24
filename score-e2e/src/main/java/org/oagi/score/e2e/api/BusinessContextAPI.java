package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;

import java.math.BigInteger;

/**
 * APIs for the business context management.
 */
public interface BusinessContextAPI {

    /**
     * Return the business context associated with the given business context name.
     *
     * @param businessContextName business context name
     * @return business context object
     */
    BusinessContextObject getBusinessContextByName(String businessContextName);

    /**
     * Create a random business context.
     *
     * @param creator account who creates this business context
     * @return a created business context object
     */
    BusinessContextObject createRandomBusinessContext(AppUserObject creator);

    /**
     * Create a random business context with the given name prefix.
     *
     * @param creator    account who creates this business context
     * @param namePrefix the prefix of the business context name
     * @return a created business context object
     */
    BusinessContextObject createRandomBusinessContext(AppUserObject creator, String namePrefix);

    void deleteBusinessContextById(BigInteger businessContext);

    void deleteRandomBusinessContextData(BusinessContextObject businessContext);


}
