package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_41_ReleaseBranchDataTypeManagementForEndUser.*;

@Suite
@SuiteDisplayName("Test Suite 41")
@SelectClasses({
        TC_41_1_DTAccess.class,
        TC_41_2_CreatingBrandNewDT.class,
        TC_41_3_EditingBrandNewEndUserDT.class,
        TC_41_4_AddBrandNewSC.class,
        TC_41_5_RemoveBrandNewSC.class,
        TC_41_6_EditingBrandNewSC.class,
        TC_41_7_EditingInheritedSCInBrandNewDTOrRevisedDT.class,
        TC_41_8_EditingValueDomains.class,
        TC_41_9_CreatingNewRevisionOfAnEndUserDT.class,
        TC_41_10_EditingRevisionOfAnEndUserDT.class,
        TC_41_11_EditingExistingSupplementaryComponentsOfRevisionOfEndUserDT.class,
        TC_41_12_EndUserDTStateManagement.class,
        TC_41_13_DeletingEndUserDT.class,
        TC_41_14_RestoringEndUserDT.class
})
public class TS_41 {
}
