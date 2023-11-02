package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.TC_15_1_AccessCoreComponentViewingEditingCommenting;
import org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.TC_15_9_EditingAssociationsDuringAnEndUserACCAmendment;
import org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.acc.*;
import org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.asccp.TC_15_10_CreatingBrandNewEndUserASCCP;
import org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.asccp.TC_15_11_EditingBrandNewEndUserASCCP;
import org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.asccp.TC_15_12_AmendEndUserASCCP;
import org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.asccp.TC_15_13_EndUserASCCPStateManagement;
import org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.bccp.*;

@Suite
@SuiteDisplayName("Test Suite 15")
@SelectClasses({
        TC_15_1_AccessCoreComponentViewingEditingCommenting.class,
        TC_15_2_CreatingBrandNewEndUserACC.class,
        TC_15_3_EditingBrandNewEndUserACC.class,
        TC_15_4_AmendEndUserACC.class,
        TC_15_5_EndUserACCStateManagement.class,
        TC_15_6_DeletingEndUserACC.class,
        TC_15_7_RestoringEndUserACC.class,
        TC_15_8_EditingAssociationsBrandNewEndUserACC.class,
        TC_15_9_EditingAssociationsDuringAnEndUserACCAmendment.class,
        TC_15_10_CreatingBrandNewEndUserASCCP.class,
        TC_15_11_EditingBrandNewEndUserASCCP.class,
        TC_15_12_AmendEndUserASCCP.class,
        TC_15_13_EndUserASCCPStateManagement.class,
        TC_15_14_CreatingBrandNewEndUserBCCP.class,
        TC_15_15_EditingBrandNewEndUserBCCP.class,
        TC_15_16_AmendEndUserBCCP.class,
        TC_15_17_EndUserBCCPStateManagement.class,
        TC_15_18_DeletingEndUserBCCP.class,
        TC_15_19_RestoringEndUserBCCP.class
})
public class TS_15 {
}
