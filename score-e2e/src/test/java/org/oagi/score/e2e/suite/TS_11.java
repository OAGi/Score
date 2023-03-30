package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper.*;

@Suite
@SuiteDisplayName("Test Suite 11")
@SelectClasses({
        TC_11_1_CodeListAccess.class,
        TC_11_2_CreatingABrandNewDeveloperCodeList.class,
        TC_11_3_EditingABrandNewDeveloperCodeList.class,
        TC_11_4_CreatingANewRevisionOfADeveloperCodeList.class,
        TC_11_5_EditingARevisionOfADeveloperCodeList.class,
        TC_11_6_DeveloperCodeListStateManagement.class,
        TC_11_7_DeletingACodeList.class,
        TC_11_8_RestoringDeveloperCodeList.class
})
public class TS_11 {
}
