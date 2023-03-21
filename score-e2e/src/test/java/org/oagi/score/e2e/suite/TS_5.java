package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_5_OAGISDeveloperAccessRightToScoreCoreFunctions.*;

@Suite
@SuiteDisplayName("Test Suite 5")
@SelectClasses({
        TC_5_1_OAGISDevelopersAuthorizedManagementOfContextCategories.class,
        TC_5_2_OAGISDevelopersAuthorizedManagementOfContextSchemes.class,
        TC_5_3_OAGISDevelopersAuthorizedManagementOfBusinessContexts.class,
        TC_5_5_OAGISDeveloperAuthorizedManagementBIE.class,
        TC_5_6_OAGISDeveloperAuthorizedAccessToBIEExpressionGeneration.class
})
public class TS_5 {
}
