package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_44_BIEInheritance.TC_44_1_CreateInheritedBIE;

@Suite
@SuiteDisplayName("Test Suite 44")
@SelectClasses({
        TC_44_1_CreateInheritedBIE.class
})
public class TS_44 {
}
