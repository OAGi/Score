package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper.TC_11_1_CodeListAccess;
import org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper.TC_11_2_CreatingABrandNewDeveloperCodeList;
import org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper.TC_11_3_EditingABrandNewDeveloperCodeList;

@Suite
@SuiteDisplayName("Test Suite 11")
@SelectClasses({
        TC_11_1_CodeListAccess.class,
        TC_11_2_CreatingABrandNewDeveloperCodeList.class,
        TC_11_3_EditingABrandNewDeveloperCodeList.class
})
public class TS_11 {
}
