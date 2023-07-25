package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_33_CodeListUplifting.TC_33_1_DeveloperCodeListUplifting;
import org.oagi.score.e2e.TS_33_CodeListUplifting.TC_33_2_EndUserCodeListUplifting;


@Suite
@SuiteDisplayName("Test Suite 33")
@SelectClasses({
        TC_33_1_DeveloperCodeListUplifting.class,
        TC_33_2_EndUserCodeListUplifting.class
})
public class TS_33 {
}
