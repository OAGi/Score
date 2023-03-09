package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_42_BusinessTerm.TC_42_1_EndUserViewOrEditBusinessTerm;
import org.oagi.score.e2e.TS_42_BusinessTerm.TC_42_2_BusinessTermAssignment;
import org.oagi.score.e2e.TS_42_BusinessTerm.TC_42_3_BusinessTermFromBIEDetailPage;
import org.oagi.score.e2e.TS_42_BusinessTerm.TC_42_4_LoadBusinessTermsFromExternalSource;

@Suite
@SuiteDisplayName("Test Suite 28")
@SelectClasses({
        TC_42_1_EndUserViewOrEditBusinessTerm.class,
        TC_42_2_BusinessTermAssignment.class,
        TC_42_3_BusinessTermFromBIEDetailPage.class,
        TC_42_4_LoadBusinessTermsFromExternalSource.class
})
public class TS_42 {
}
