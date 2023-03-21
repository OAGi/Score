package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_2_OAGISDeveloperCanManageUsers.TC_2_1_OAGISDeveloperCanManageOAGISDeveloperAccounts;
import org.oagi.score.e2e.TS_2_OAGISDeveloperCanManageUsers.TC_2_2_OAGISDeveloperCanManageEndUserAccounts;

@Suite
@SuiteDisplayName("Test Suite 2")
@SelectClasses({
        TC_2_1_OAGISDeveloperCanManageOAGISDeveloperAccounts.class,
        TC_2_2_OAGISDeveloperCanManageEndUserAccounts.class
})
public class TS_2 {
}
