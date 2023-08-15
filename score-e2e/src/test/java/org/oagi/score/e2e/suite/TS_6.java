package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_6_EndUserAccessRightScoreCoreFunctions.TC_6_1_EndUserAuthorizedManagementContextSchemes;
import org.oagi.score.e2e.TS_6_EndUserAccessRightScoreCoreFunctions.TC_6_2_EndUserAuthorizedManagementBIE;
import org.oagi.score.e2e.TS_6_EndUserAccessRightScoreCoreFunctions.TC_6_2_EndUserAuthorizedManagementBIE_Global_Extension;
import org.oagi.score.e2e.TS_6_EndUserAccessRightScoreCoreFunctions.TC_6_3_EndUserAuthorizedAccessToBIEExpressionGeneration;


@Suite
@SuiteDisplayName("Test Suite 6")
@SelectClasses({
        TC_6_1_EndUserAuthorizedManagementContextSchemes.class,
        TC_6_2_EndUserAuthorizedManagementBIE.class,
        TC_6_2_EndUserAuthorizedManagementBIE_Global_Extension.class,
        TC_6_3_EndUserAuthorizedAccessToBIEExpressionGeneration.class
})
public class TS_6 {
}
