package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_28_HomePage.TC_28_1_BIEsTab;
import org.oagi.score.e2e.TS_28_HomePage.TC_28_2_UserExtensionsTabForDevelopers;
import org.oagi.score.e2e.TS_28_HomePage.TC_28_3_UserExtensionsTabForEndUsers;

@Suite
@SuiteDisplayName("Test Suite 28")
@SelectClasses({
        TC_28_1_BIEsTab.class,
        TC_28_2_UserExtensionsTabForDevelopers.class,
        TC_28_3_UserExtensionsTabForEndUsers.class
})
public class TS_28 {
}
