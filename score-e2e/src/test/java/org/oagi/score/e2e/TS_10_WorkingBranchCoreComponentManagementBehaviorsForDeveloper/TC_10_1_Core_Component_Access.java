package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;

import java.util.ArrayList;
import java.util.List;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_1_Core_Component_Access extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_10_1_TA_1")
    public void developer_can_see_all_cc_owned_by_any_developer_in_any_state_in_cc_list_page(){

    }

    @Test
    @DisplayName("TC_10_1_TA_2")
    public void developer_can_view_and_edit_the_details_of_a_cc_that_is_in_wip_state_and_owned_by_him(){

    }

    @Test
    @DisplayName("TC_10_1_TA_3")
    public void developer_can_view_but_cannot_edit_the_details_of_a_cc_that_is_in_wip_state_and_owned_by_another_developer(){

    }

    @Test
    @DisplayName("TC_10_1_TA_4")
    public void test_TA_4(){

    }

    @Test
    @DisplayName("TC_10_1_TA_5")
    public void test_TA_5(){

    }

    @Test
    @DisplayName("TC_10_1_TA_6")
    public void test_TA_6(){

    }

    @Test
    @DisplayName("TC_10_1_TA_7")
    public void test_TA_7(){

    }

    @Test
    @DisplayName("TC_10_1_TA_8")
    public void test_TA_8(){

    }

    @Test
    @DisplayName("TC_10_1_TA_9")
    public void test_TA_9(){

    }

    @Test
    @DisplayName("TC_10_1_TA_10")
    public void test_TA_10(){

    }
    @Test
    @DisplayName("TC_10_1_TA_11")
    public void test_TA_11(){

    }

    @Test
    @DisplayName("TC_10_1_TA_12")
    public void test_TA_12(){

    }

    @Test
    @DisplayName("TC_10_1_TA_13")
    public void test_TA_13(){

    }
    @Test
    @DisplayName("TC_10_1_TA_14")
    public void test_TA_14(){

    }

    @Test
    @DisplayName("TC_10_1_TA_15")
    public void test_TA_15(){

    }

    @Test
    @DisplayName("TC_10_1_TA_16")
    public void test_TA_16(){

    }

    @Test
    @DisplayName("TC_10_1_TA_17")
    public void test_TA_17(){

    }

    @Test
    @DisplayName("TC_10_1_TA_18")
    public void test_TA_18(){

    }

    @Test
    @DisplayName("TC_10_1_TA_19")
    public void test_TA_19(){

    }

    @Test
    @DisplayName("TC_10_1_TA_20")
    public void test_TA_20(){

    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }


}
