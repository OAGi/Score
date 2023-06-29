package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper.TC_38_1_DTAccess;
import org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper.TC_38_2_CreatingBrandNewDT;

@Suite
@SuiteDisplayName("Test Suite 38")
@SelectClasses({
        TC_38_1_DTAccess.class,
        TC_38_2_CreatingBrandNewDT.class,
        TC_38_3_EditingBrandNewDeveloperDT.class,
        TC_38_4_AddBrandNewSC.class,
        TC_38_5_RemoveBrandNewSC.class,
        TC_38_6_EditingBrandNewSC.class,
        TC_38_7_EditingInheritedSCInBrandNewDTOrRevisedDT.class,
        TC_38_8_EditingValueDomains.class,
        TC_38_9_CreatingNewRevisionOfDeveloperDT.class,
        TC_38_10_EditingRevisionOfDeveloperDT.class,
        TC_38_11_EditingExistingSupplementaryComponentsOfRevisionOfDeveloperDT.class,
        TC_38_12_DeveloperDTStateManagement.class,
        TC_38_13_DeletingDeveloperDT.class,
        TC_38_14_RestoringDeveloperDT.class
})
public class TS_38 {
}
