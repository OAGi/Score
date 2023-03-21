package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.ASCCPObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;

import java.math.BigInteger;
import java.util.List;

/**
 * APIs for the business information entity (BIE) management.
 */
public interface BusinessInformationEntityAPI {

    TopLevelASBIEPObject generateRandomTopLevelASBIEP(List<BusinessContextObject> businessContexts,
                                                      ASCCPObject asccp, AppUserObject creator, String state);

    TopLevelASBIEPObject getTopLevelASBIEPByID(BigInteger topLevelAsbiepId);

    TopLevelASBIEPObject getTopLevelASBIEPByDENAndReleaseNum(String den, String branch);

    void updateTopLevelASBIEP(TopLevelASBIEPObject topLevelASBIEP);

    void deleteTopLevelASBIEPByTopLevelASBIEPId(TopLevelASBIEPObject topLevelAsbiep);

}
