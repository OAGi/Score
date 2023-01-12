package org.oagi.score.e2e.suite;


import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_27_OAGISDeveloperVisitAboutPage.TC_27_1_AboutPageContainsFollowingInformation;

@Suite
@SuiteDisplayName("Test Suite 27")
@SelectClasses({
        TC_27_1_AboutPageContainsFollowingInformation.class
})
public class TS_27 {
}
