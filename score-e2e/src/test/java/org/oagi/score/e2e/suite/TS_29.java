package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_29_BIEUplifting.TC_29_1_BIEUplifting;

@Suite
@SuiteDisplayName("Test Suite 29")
@SelectClasses({
        TC_29_1_BIEUplifting.class
})
public class TS_29 {
}
