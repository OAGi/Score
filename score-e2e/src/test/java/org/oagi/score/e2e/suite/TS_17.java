package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser.*;

@Suite
@SuiteDisplayName("Test Suite 17")
@SelectClasses({
        TC_17_1_CodeListAccess.class,
        TC_17_2_CreatingABrandNewEndUserCodeList.class,
        TC_17_3_EditingABrandNewEndUserCodeList.class,
        TC_17_4_AmendAnEndUserCodeList.class,
        TC_17_5_EndUserCodeListStateManagement.class,
        TC_17_6_DeletingACodeList.class,
        TC_17_7_RestoringEndUserCodeList.class
})
public class TS_17 {
}
