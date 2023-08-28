package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_36_ReleaseBranchAgencyIDListManagementDeveloper.TC_36_1_AgencyIdListAccess;

@Suite
@SuiteDisplayName("Test Suite 36")
@SelectClasses({
        TC_36_1_AgencyIdListAccess.class
})
public class TS_36 {
}
