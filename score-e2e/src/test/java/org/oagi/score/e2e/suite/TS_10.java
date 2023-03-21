package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.TC_10_7_EditingAssociationsOfARevisionOfADeveloperACC;
import org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.bccp.*;

@Suite
@SuiteDisplayName("Test Suite 10")
@SelectClasses({
        TC_10_7_EditingAssociationsOfARevisionOfADeveloperACC.class,
        TC_10_18_CreatingBrandNewDeveloperBCCP.class,
        TC_10_19_EditingBrandNewDeveloperBCCP.class,
        TC_10_20_CreatingNewRevisionDeveloperBCCP.class,
        TC_10_21_EditingRevisionDeveloperBCCP.class,
        TC_10_22_DeveloperBCCPStateManagement.class,
        TC_10_23_DeletingDeveloperBCCP.class,
        TC_10_24_RestoringDeveloperBCCP.class
})
public class TS_10 {
}
