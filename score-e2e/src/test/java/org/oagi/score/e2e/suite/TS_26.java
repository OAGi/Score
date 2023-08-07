package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_26_UserGuideIsAccessbile.TC_26_1_UserGuideIsAccessible;

@Suite
@SuiteDisplayName("Test Suite 26")
@SelectClasses({
        TC_26_1_UserGuideIsAccessible.class
})
public class TS_26 {
}
