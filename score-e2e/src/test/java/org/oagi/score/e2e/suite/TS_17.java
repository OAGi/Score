package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser.TC_17_1_CodeListAccess;
import org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser.TC_17_2_CreatingABrandNewEndUserCodeList;
import org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser.TC_17_3_EditingABrandNewEndUserCodeList;
import org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser.TC_17_4_AmendAnEndUserCodeList;

@Suite
@SuiteDisplayName("Test Suite 17")
@SelectClasses({
        TC_17_1_CodeListAccess.class,
        TC_17_2_CreatingABrandNewEndUserCodeList.class,
        TC_17_3_EditingABrandNewEndUserCodeList.class,
        TC_17_4_AmendAnEndUserCodeList.class
})
public class TS_17 {
}
