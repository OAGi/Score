package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_42_2_BusinessTermAssignment extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_42_2_1")
    public void enduser_should_open_page_titled_business_term_assignment_under_bie_menu() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        getDriver().manage().window().maximize();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        String businessTermAssignmentPageTitle = getText(bieMenu.openBusinessTermAssignmentSubMenu().getTitle());
        assertEquals("Business Term Assignment", businessTermAssignmentPageTitle);
    }

    @Test
    @DisplayName("TC_42_2_2")
    public void enduser_can_view_all_business_terms_with_assignments_on_business_term_assignment_page() {


    }

    @Test
    @DisplayName("TC_42_2_3")
    public void enduser_can_view_asbies_bbies_and_toplevelbies_on_business_term_assignment_page() {
    }

    @Test
    @DisplayName("TC_42_2_4")
    public void enduser_can_search_business_term_assignments_by_bietype_and_den_or_business_term_or_uri_or_typecode_on_business_term_assigment_page() {
    }

    @Test
    @DisplayName("TC_42_2_5")
    public void enduser_can_filter_only_preferred_business_terms_on_business_term_assignment_page() {
    }

    @Test
    @DisplayName("TC_42_2_6")
    public void enduser_can_select_bie_to_view_all_business_term_assignments_assigned_for_that_bie_on_business_term_assignment_page() {
    }

    @Test
    @DisplayName("TC_42_2_7")
    public void enduser_can_select_bie_to_view_all_business_term_not_assigned_but_available_for_that_bie_on_assign_business_term_page() {
    }

    @Test
    @DisplayName("TC_42_2_8")
    public void enduser_can_filter_business_terms_already_assigned_to_the_same_core_component_on_assign_business_term_page() {
    }

    @Test
    @DisplayName("TC_42_2_9")
    public void enduser_can_assign_duplicate_business_term_and_type_code_based_on_mixed_conditions_on_assign_business_term_page() {
    }

    @Test
    @DisplayName("TC_42_2_10")
    public void enduser_can_only_set_one_preferred_business_term_assignment_for_each_bie_on_assign_business_term_page() {
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