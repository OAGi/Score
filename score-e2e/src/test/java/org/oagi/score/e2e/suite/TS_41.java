package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_41_ReleaseBranchDataTypeManagementForEndUser.TC_41_1_DTAccess;

@Suite
@SuiteDisplayName("Test Suite 41")
@SelectClasses({
        TC_41_1_DTAccess.class
})
public class TS_41 {
}
