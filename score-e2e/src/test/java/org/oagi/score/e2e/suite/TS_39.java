package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_39_ReleaseBranchDataTypeManagementForDeveloper.TC_39_1_AccessToDTViewingEditingAndCommenting;

@Suite
@SuiteDisplayName("Test Suite 39")
@SelectClasses({
        TC_39_1_AccessToDTViewingEditingAndCommenting.class
})
public class TS_39 {
}
