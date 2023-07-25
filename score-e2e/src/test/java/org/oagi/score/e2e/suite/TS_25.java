package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_25_DeveloperBIEManagement.TC_25_1_ReuseBIE;
import org.oagi.score.e2e.TS_25_DeveloperBIEManagement.TC_25_2_CreateTopLevelBIEFromBIENode;

@Suite
@SuiteDisplayName("Test Suite 25")
@SelectClasses({
        TC_25_1_ReuseBIE.class,
        TC_25_2_CreateTopLevelBIEFromBIENode.class
})
public class TS_25 {
}
