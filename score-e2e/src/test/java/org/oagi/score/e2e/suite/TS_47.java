package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_47_BIEInverseMode.TC_47_1_AdminManagementOfBIEInverseMode;
import org.oagi.score.e2e.TS_47_BIEInverseMode.TC_47_4_RecursiveBIEInverseModeExpression;

@Suite
@SuiteDisplayName("Test Suite 47")
@SelectClasses({
        TC_47_1_AdminManagementOfBIEInverseMode.class,
        TC_47_4_RecursiveBIEInverseModeExpression.class
})
public class TS_47 {
}
