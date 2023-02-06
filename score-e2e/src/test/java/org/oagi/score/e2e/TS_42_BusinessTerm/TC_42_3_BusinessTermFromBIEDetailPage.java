package org.oagi.score.e2e.TS_42_BusinessTerm;

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
public class TC_42_3_BusinessTermFromBIEDetailPage extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_42_3_1")
    public void end_user_can_see_all_business_terms_assigned_to_the_descendant_bie_node() {
    }

    @Test
    @DisplayName("TC_42_3_2")
    public void end_user_can_hover_over_show_business_terms_button_view_up_to_five_business_term_assigned_in_the_descendent_bie_panel() {
    }

    @Test
    @DisplayName("TC_42_3_3")
    public void end_user_can_click_assign_business_term_button_in_descendent_bie_panel_assign_business_terms() {
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
