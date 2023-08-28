package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_35_WorkingBranchAgencyIDListManagementEndUser.TC_35_1_AgencyIdListAccess;

@Suite
@SuiteDisplayName("Test Suite 35")
@SelectClasses({
        TC_35_1_AgencyIdListAccess.class
})
public class TS_35 {
}
