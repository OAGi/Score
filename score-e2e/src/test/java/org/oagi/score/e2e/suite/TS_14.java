package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_14_WorkingBranchCoreComponentManagementBehaviorsForEndUser.TC_14_1_AccessToCoreComponentViewingEditingAndCommenting;

@Suite
@SuiteDisplayName("Test Suite 14")
@SelectClasses({
        TC_14_1_AccessToCoreComponentViewingEditingAndCommenting.class
})
public class TS_14 {
}
