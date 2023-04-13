package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser.TC_17_1_CodeListAccess;
import org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser.TC_17_2_CreatingABrandNewEndUserCodeList;

@Suite
@SuiteDisplayName("Test Suite 17")
@SelectClasses({
        TC_17_1_CodeListAccess.class,
        TC_17_2_CreatingABrandNewEndUserCodeList.class
})
public class TS_17 {
}
