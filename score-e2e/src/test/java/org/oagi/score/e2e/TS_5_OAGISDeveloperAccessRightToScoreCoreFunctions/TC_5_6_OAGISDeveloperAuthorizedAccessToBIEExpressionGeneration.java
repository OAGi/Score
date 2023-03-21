package org.oagi.score.e2e.TS_5_OAGISDeveloperAccessRightToScoreCoreFunctions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;

@Execution(ExecutionMode.CONCURRENT)
public class TC_5_6_OAGISDeveloperAuthorizedAccessToBIEExpressionGeneration extends BaseTest {

    private AppUserObject appUser;

    @BeforeEach
    public void init() {
        super.init();

        // Create random end-user
        appUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
    }

    @Test
    @DisplayName("TC_5_6_TA_1")
    public void test_TA_1() {
    }

    @Test
    @DisplayName("TC_5_6_TA_2")
    public void test_TA_2() {
    }

    @Test
    @DisplayName("TC_5_6_TA_3")
    public void test_TA_3() {
    }

    @Test
    @DisplayName("TC_5_6_TA_4")
    public void test_TA_4() {
    }

    @Test
    @DisplayName("TC_5_6_TA_5")
    public void test_TA_5() {
    }

    @Test
    @DisplayName("TC_5_6_TA_6")
    public void test_TA_6() {
    }

    @Test
    @DisplayName("TC_5_6_TA_7")
    public void test_TA_7() {
    }

    @Test
    @DisplayName("TC_5_6_TA_8")
    public void test_TA_8() {
    }

    @Test
    @DisplayName("TC_5_6_TA_9")
    public void test_TA_9() {
    }

    @Test
    @DisplayName("TC_5_6_TA_10")
    public void test_TA_10() {
    }

    @Test
    @DisplayName("TC_5_6_TA_11")
    public void test_TA_11() {
    }

    @Test
    @DisplayName("TC_5_6_TA_12")
    public void test_TA_12() {
    }

    @Test
    @DisplayName("TC_5_6_TA_13")
    public void test_TA_13() {
    }

    @Test
    @DisplayName("TC_5_6_TA_14")
    public void test_TA_14() {
    }

    @Test
    @DisplayName("TC_5_6_TA_15")
    public void test_TA_15() {
    }

    @Test
    @DisplayName("TC_5_6_TA_16")
    public void test_TA_16() {
    }

    @Test
    @DisplayName("TC_5_6_TA_17")
    public void test_TA_17() {
    }

    @Test
    @DisplayName("TC_5_6_TA_18")
    public void test_TA_18() {
    }

    @Test
    @DisplayName("TC_5_6_TA_19")
    public void test_TA_19() {
    }

    @Test
    @DisplayName("TC_5_6_TA_20")
    public void test_TA_20() {
    }

    @Test
    @DisplayName("TC_5_6_TA_21")
    public void test_TA_21() {
    }

    @Test
    @DisplayName("TC_5_6_TA_22")
    public void test_TA_22() {
    }

    @Test
    @DisplayName("TC_5_6_TA_23")
    public void test_TA_23() {
    }

    @Test
    @DisplayName("TC_5_6_TA_24")
    public void test_TA_24() {
    }

    @Test
    @DisplayName("TC_5_6_TA_25")
    public void test_TA_25() {
    }

    @Test
    @DisplayName("TC_5_6_TA_26")
    public void test_TA_26() {
    }

    @Test
    @DisplayName("TC_5_6_TA_27")
    public void test_TA_27() {
    }

    @Test
    @DisplayName("TC_5_6_TA_28")
    public void test_TA_28() {
    }

    @Test
    @DisplayName("TC_5_6_TA_29")
    public void test_TA_29() {
    }

    @Test
    @DisplayName("TC_5_6_TA_30")
    public void test_TA_30() {
    }

    @Test
    @DisplayName("TC_5_6_TA_31")
    public void test_TA_31() {
    }

    @Test
    @DisplayName("TC_5_6_TA_32")
    public void test_TA_32() {
    }

    @Test
    @DisplayName("TC_5_6_TA_33")
    public void test_TA_33() {
    }

    @Test
    @DisplayName("TC_5_6_TA_34")
    public void test_TA_34() {
    }

    @Test
    @DisplayName("TC_5_6_TA_35")
    public void test_TA_35() {
    }

    @Test
    @DisplayName("TC_5_6_TA_36")
    public void test_TA_36() {
    }

    @Test
    @DisplayName("TC_5_6_TA_37")
    public void test_TA_37() {
    }

    @Test
    @DisplayName("TC_5_6_TA_38")
    public void test_TA_38() {
    }

    @Test
    @DisplayName("TC_5_6_TA_39")
    public void test_TA_39() {
    }

    @Test
    @DisplayName("TC_5_6_TA_40")
    public void test_TA_40() {
    }

    @Test
    @DisplayName("TC_5_6_TA_41")
    public void test_TA_41() {
    }

    @Test
    @DisplayName("TC_5_6_TA_42")
    public void test_TA_42() {
    }

    @Test
    @DisplayName("TC_5_6_TA_43")
    public void test_TA_43() {
    }

    @Test
    @DisplayName("TC_5_6_TA_44")
    public void test_TA_44() {
    }

    @Test
    @DisplayName("TC_5_6_TA_45")
    public void test_TA_45() {
    }

    @Test
    @DisplayName("TC_5_6_TA_46")
    public void test_TA_46() {
    }

    @Test
    @DisplayName("TC_5_6_TA_Test_Refreshing_page")
    public void test_TA_Test_Refreshing_page() {
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random developer
        if (appUser != null) {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(appUser.getLoginId());
        }
    }

}
