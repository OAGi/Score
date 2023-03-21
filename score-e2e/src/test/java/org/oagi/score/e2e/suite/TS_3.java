package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_3_EndUserAuthorizedFunctions.TC_3_1_EndUsersAuthorizedFunctionalities;

@Suite
@SuiteDisplayName("Test Suite 3")
@SelectClasses({
        TC_3_1_EndUsersAuthorizedFunctionalities.class
})
public class TS_3 {
}
