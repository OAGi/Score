package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_34_WorkingBranchAgencyIDListManagementforDeveloper.*;

@Suite
@SuiteDisplayName("Test Suite 34")
@SelectClasses({
        TC_34_1_AgencyIdListAccess.class,
        TC_34_2_CreatingBrandNewDeveloperAgencyIDList.class,
        TC_34_3_CreatingNewRevisionDeveloperAgencyIDList.class,
        TC_34_4_EditingRevisionDeveloperAgencyIDList.class,
        TC_34_5_DeveloperAgencyIDListStateManagement.class,
        TC_34_6_DeletingAgencyIDList.class
})
public class TS_34 {
}
