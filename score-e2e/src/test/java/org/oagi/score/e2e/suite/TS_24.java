package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_24_EndUserBIEManagement.TC_24_1_ReuseBIE;
import org.oagi.score.e2e.TS_24_EndUserBIEManagement.TC_24_2_CreateTopLevelBIEFromBIENode;

@Suite
@SuiteDisplayName("Test Suite 24")
@SelectClasses({
        TC_24_1_ReuseBIE.class,
        TC_24_2_CreateTopLevelBIEFromBIENode.class
})
public class TS_24 {
}
