package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_40_WorkingBranchDataTypeManagementForEndUser.TC_40_1_AccessToDTViewingEditingAndCommenting;

@Suite
@SuiteDisplayName("Test Suite 40")
@SelectClasses({
        TC_40_1_AccessToDTViewingEditingAndCommenting.class
})
public class TS_40 {
}
