package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_16_ReleaseBranchCodeListManagementForDeveloper.TC_16_1_CodeListAccess;

@Suite
@SuiteDisplayName("Test Suite 16")
@SelectClasses({
        TC_16_1_CodeListAccess.class
})
public class TS_16 {
}
