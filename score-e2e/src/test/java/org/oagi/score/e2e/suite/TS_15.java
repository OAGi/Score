package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.TC_15_9_EditingAssociationsDuringAnEndUserACCAmendment;

@Suite
@SuiteDisplayName("Test Suite 15")
@SelectClasses({
        TC_15_9_EditingAssociationsDuringAnEndUserACCAmendment.class
})
public class TS_15 {
}
