package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_1_OAGISDeveloperAuthenticationAndAuthorizedFunctions.TC_1_1_BuiltInOAGIDeveloperAccountExists;
import org.oagi.score.e2e.TS_1_OAGISDeveloperAuthenticationAndAuthorizedFunctions.TC_1_2_OAGISDevelopersAuthorizedFunctionalities;

@Suite
@SuiteDisplayName("Test Suite 1")
@SelectClasses({
        TC_1_1_BuiltInOAGIDeveloperAccountExists.class,
        TC_1_2_OAGISDevelopersAuthorizedFunctionalities.class
})
public class TS_1 {
}
