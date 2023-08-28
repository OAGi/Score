package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_20_NamespaceManagement.TC_20_1_DeveloperManagementOfNamespaces;
import org.oagi.score.e2e.TS_20_NamespaceManagement.TC_20_2_EndUserManagementfNamespaces;

@Suite
@SuiteDisplayName("Test Suite 20")
@SelectClasses({
        TC_20_1_DeveloperManagementOfNamespaces.class,
        TC_20_2_EndUserManagementfNamespaces.class
})
public class TS_20 {
}
