package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_32_HistoryFunctionality.TC_32_1_ACC_History;

@Suite
@SuiteDisplayName("Test Suite 32")
@SelectClasses({
        TC_32_1_ACC_History.class
})
public class TS_32 {
}
