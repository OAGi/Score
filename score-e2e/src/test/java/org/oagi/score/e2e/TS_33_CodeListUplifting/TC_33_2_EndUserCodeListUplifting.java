package org.oagi.score.e2e.TS_33_CodeListUplifting;

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
public class TC_33_2_EndUserCodeListUplifting extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_33_2_1")
    public void end_user_can_choose_code_list_to_uplift_from_source_release_to_target_release_in_uplift_code_list_page() {
    }

    @Test
    @DisplayName("TC_33_2_2")
    public void end_user_can_view_any_code_list_by_state_in_uplift_code_list_page() {
    }


    @Test
    @DisplayName("TC_33_2_3")
    public void end_user_can_uplift_code_list_in_wip_state_from_source_release_to_target_release() {
    }

    @Test
    @DisplayName("TC_33_2_4")
    public void end_user_can_uplift_code_list_in_qa_state_from_source_release_to_target_release() {
    }

    @Test
    @DisplayName("TC_33_2_5")
    public void end_user_can_uplift_code_list_in_production_state_from_source_release_to_target_release() {
    }

    @Test
    @DisplayName("TC_33_2_6")
    public void end_user_can_uplift_code_list_in_deleted_state_from_source_release_to_target_release() {
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

}
