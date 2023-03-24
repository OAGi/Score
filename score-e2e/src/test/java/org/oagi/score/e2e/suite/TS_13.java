package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_13_ReleaseBranchCoreComponentManagementBehaviorForOAGISDeveloper.TC_13_1_AccessToCoreComponentViewingEditingAndCommenting;

@Suite
@SuiteDisplayName("Test Suite 13")
@SelectClasses({
        TC_13_1_AccessToCoreComponentViewingEditingAndCommenting.class
})
public class TS_13 {
}
