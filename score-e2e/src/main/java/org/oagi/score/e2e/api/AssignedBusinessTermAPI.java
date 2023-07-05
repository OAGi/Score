package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.ABIEObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.AssignedBusinessTermObject;
import org.oagi.score.e2e.obj.BusinessTermObject;

import java.math.BigInteger;

/**
 * APIs for the assigned business term assignment.
 */
public interface AssignedBusinessTermAPI {
    /**
     * Return the assigned business term associated with the given business term name.
     *
     * @param businessTermName business term name
     * @return assigned business term object
     */
    AssignedBusinessTermObject getAssignedBusinessTermByName(String businessTermName);

    /**
     * Return the assigned business term associated with the given BIE name
     *
     * @param bieName BIE name
     * @return a list of assigned business term object
     */
    AssignedBusinessTermObject[] getAssignedBusinessTermByBIE(String bieName);

    /**
     * Create the assigned business term as requested
     *
     * @param businessTerm business term object
     * @param aBIE         ABIE or BBIE object
     * @param creator      account who creates this assigned business term
     * @return a created assigned business term object
     */
    AssignedBusinessTermObject createRandomAssignedBusinessTerm(BusinessTermObject businessTerm, ABIEObject aBIE,
                                                                AppUserObject creator);


    void deleteAssignedBusinessTermById(BigInteger businessTermID);

}
