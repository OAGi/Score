package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_7_UITerminology.*;

@Suite
@SuiteDisplayName("Test Suite 7")
@SelectClasses({
        TC_7_1_OAGiNavigationMenu.class,
        TC_7_2_OAGiTerminologyViewEditBIEPage.class,
        TC_7_3_OAGiTerminologyCreateBIEForSelectBusinessContextsPage.class,
        TC_7_4_OAGiTerminologyCopyBIEForSelectBusinessContextsPage.class,
        TC_7_5_OAGiTerminologyAppendAssociationDialog.class,
        TC_7_6_OAGiTerminologyCoreComponent.class
})
public class TS_7 {
}
