package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_4_EndUsersProfileManagement.TC_4_1_EndUsersProfileManagement;

@Suite
@SuiteDisplayName("Test Suite 4")
@SelectClasses({
        TC_4_1_EndUsersProfileManagement.class
})
public class TS_4 {
}
