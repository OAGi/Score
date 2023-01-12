package org.oagi.score.e2e.suite;


import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_9_DataRetention.TC_9_1_NoUserAccountCanBeDeleted;

@Suite
@SuiteDisplayName("Test Suite 9")
@SelectClasses({
        TC_9_1_NoUserAccountCanBeDeleted.class
})
public class TS_9 {
}
