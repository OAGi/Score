package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_18_DraftReleaseBranchCoreComponentCodeListAccessDevelopersEndUsers.TC_18_1_CoreComponentAccess;
import org.oagi.score.e2e.TS_18_DraftReleaseBranchCoreComponentCodeListAccessDevelopersEndUsers.TC_18_2_CodeListAccess;

@Suite
@SuiteDisplayName("Test Suite 18")
@SelectClasses({
        TC_18_1_CoreComponentAccess.class,
        TC_18_2_CodeListAccess.class
})
public class TS_18 {
}
