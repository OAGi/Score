package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_44_BIEInheritance.TC_44_1_CreateInheritedBIE;
import org.oagi.score.e2e.TS_44_BIEInheritance.TC_44_2_UseBaseBIE;
import org.oagi.score.e2e.TS_44_BIEInheritance.TC_44_3_CreateInheritedBIEWithBaseReusedBIE;
import org.oagi.score.e2e.TS_44_BIEInheritance.TC_44_4_StateChangeRules;

@Suite
@SuiteDisplayName("Test Suite 44")
@SelectClasses({
        TC_44_1_CreateInheritedBIE.class,
        TC_44_2_UseBaseBIE.class,
        TC_44_3_CreateInheritedBIEWithBaseReusedBIE.class,
        TC_44_4_StateChangeRules.class
})
public class TS_44 {
}
