package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessTermObject;

import java.math.BigInteger;

/**
 * APIs for the business term management.
 */
public interface BusinessTermAPI {
    /**
     * Return the business term associated with the given business term name.
     *
     * @param businessTermName business term name
     * @return business term object
     */
    BusinessTermObject getBusinessTermByName(String businessTermName);

    /**
     * Create a random business term.
     *
     * @param creator account who creates this business context
     * @return a created business term object
     */
    BusinessTermObject createRandomBusinessTerm(AppUserObject creator);

    /**
     * Create the business term as requested.
     *
     * @param businessTerm business term object
     * @return a created business term object
     */
    BusinessTermObject createBusinessTerm(BusinessTermObject businessTerm);

    /**
     * Create a random business term with the given name prefix.
     *
     * @param creator    account who creates this business context
     * @param namePrefix the prefix of the business term name
     * @return a created business term object
     */
    BusinessTermObject createRandomBusinessTerm(AppUserObject creator, String namePrefix);

    /**
     * Create the business term as requested.
     *
     * @param businessTerm business term object
     * @param creator      account who creates this business term
     * @return a created business term object
     */
    BusinessTermObject createRandomBusinessTerm(BusinessTermObject businessTerm,
                                                AppUserObject creator);

    void deleteBusinessTermById(BigInteger businessTerm);

}
