package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_21_ModuleManagement.TC_21_1_ManageModuleSet;
import org.oagi.score.e2e.TS_21_ModuleManagement.TC_21_2_ManageReleaseModuleSet;
import org.oagi.score.e2e.TS_21_ModuleManagement.TC_21_3_ManageCCModuleAssignment;


@Suite
@SuiteDisplayName("Test Suite 21")
@SelectClasses({
        TC_21_1_ManageModuleSet.class,
        TC_21_2_ManageReleaseModuleSet.class,
        TC_21_3_ManageCCModuleAssignment.class
})
public class TS_21 {
}
