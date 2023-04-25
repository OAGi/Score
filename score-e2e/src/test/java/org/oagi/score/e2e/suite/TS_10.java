package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.TC_10_1_Core_Component_Access;
import org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.acc.*;
import org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.asccp.*;
import org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.bccp.*;

@Suite
@SuiteDisplayName("Test Suite 10")
@SelectClasses({
        TC_10_1_Core_Component_Access.class,
        TC_10_2_CreatingBrandNewDeveloperACC.class,
        TC_10_3_EditingBrandNewDeveloperACC.class,
        TC_10_4_EditingAssociationsBrandNewDeveloperACC.class,
        TC_10_5_CreatingNewRevisionDeveloperACC.class,
        TC_10_6_EditingRevisionDeveloperACC.class,
        TC_10_7_EditingAssociationsRevisionDeveloperACC.class,
        TC_10_8_DeveloperACCStateManagement.class,
        TC_10_9_DeletingDeveloperACC.class,
        TC_10_10_RestoringDeveloperACC.class,
        TC_10_11_CreatingBrandNewDeveloperASCCP.class,
        TC_10_12_EditingBrandNewDeveloperASCCP.class,
        TC_10_13_CreatingNewRevisionDeveloperASCCP.class,
        TC_10_14_EditingRevisionDeveloperASCCP.class,
        TC_10_15_DeveloperASCCPStateManagement.class,
        TC_10_16_DeletingDeveloperASCCP.class,
        TC_10_17_RestoringDeveloperASCCP.class,
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
