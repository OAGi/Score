package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper.TC_38_1_DTAccess;
import org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper.TC_38_2_CreatingBrandNewDT;

@Suite
@SuiteDisplayName("Test Suite 38")
@SelectClasses({
        TC_38_1_DTAccess.class,
        TC_38_2_CreatingBrandNewDT.class
})
public class TS_38 {
}
