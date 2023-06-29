package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_37_ReleaseBranchAgencyIDListManagementEndUser.*;

@Suite
@SuiteDisplayName("Test Suite 37")
@SelectClasses({
        TC_37_1_AgencyIdListAccess.class,
        TC_37_2_CreatingBrandNewEndUserAgencyIDList.class,
        TC_37_3_EditingBrandNewEndUserAgencyIDList.class,
        TC_37_4_AmendEndUserAgencyIDList.class,
        TC_37_5_EndUserAgencyIDListStateManagement.class,
        TC_37_6_DeletingEndUserAgencyIDList.class,
        TC_37_7_RestoringEndUserAgencyIDList.class
})
public class TS_37 {
}
