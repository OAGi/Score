package org.oagi.score.e2e.TS_7_UITerminology;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;


@Execution(ExecutionMode.CONCURRENT)
public class TC_7_2_OAGiTerminologyViewEditBIEPage extends BaseTest {

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_7_2_TA_1")
    @Disabled
    public void test_TA_1() {
        //The name of the page should be “BIEs (Profiled Components, Nouns, BODs)”.
    }

}
