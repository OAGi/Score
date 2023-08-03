package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_19_ReleaseManagement.TC_19_1_ReleaseManagement;

@Suite
@SuiteDisplayName("Test Suite 19")
@SelectClasses({
        TC_19_1_ReleaseManagement.class
})
public class TS_19 {
}
