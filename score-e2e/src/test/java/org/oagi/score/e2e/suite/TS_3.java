package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_3_EndUserAuthorizedFunctions.TC_3_1_EndUsersAuthorizedFunctionalities;
import org.oagi.score.e2e.TS_3_EndUserAuthorizedFunctions.TC_3_2_EndUsersAuthorizedFunctionalitiesInStandardBrowsingMode;
import org.oagi.score.e2e.TS_3_EndUserAuthorizedFunctions.TC_3_3_EndUsersAuthorizedFunctionalitiesInTenantMode;

@Suite
@SuiteDisplayName("Test Suite 3")
@SelectClasses({
        TC_3_1_EndUsersAuthorizedFunctionalities.class,
        TC_3_2_EndUsersAuthorizedFunctionalitiesInStandardBrowsingMode.class,
        TC_3_3_EndUsersAuthorizedFunctionalitiesInTenantMode.class
})
public class TS_3 {
}
