package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_46_BIEPackageManagement.TC_46_1_RevisionReasonText;
import org.oagi.score.e2e.TS_46_BIEPackageManagement.TC_46_2_BackwardCompatibilityIndicator;

@Suite
@SuiteDisplayName("Test Suite 46")
@SelectClasses({
        TC_46_1_RevisionReasonText.class,
        TC_46_2_BackwardCompatibilityIndicator.class
})
public class TS_46 {
}
