package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermBIEPage;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermBTPage;
import org.oagi.score.e2e.page.business_term.BusinessTermAssignmentPage;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;

@Execution(ExecutionMode.CONCURRENT)
public class TC_42_3_BusinessTermFromBIEDetailPage extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

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
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        //use pre-existing BBIE node
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        //Assign business term to pre-existing, used BBIE node
        assertTrue(bbiePanel.getAssignBusinessTermButton(true).isEnabled());
        AssignBusinessTermBTPage assignBusinessTermBTPage = bbiePanel.clickAssignBusinessTermButton();
        //assign up to 7 random business terms to selected BIE for testing purpose
        ArrayList<BusinessTermObject> businessTerms = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTerms.add(randomBusinessTerm);
            assignBusinessTermBTPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
            assignBusinessTermBTPage.hitSearchButton();
            click(assignBusinessTermBTPage.getSelectCheckboxAtIndex(1));
            click(assignBusinessTermBTPage.getCreateButton());
            WebElement bbieNodeForLoop = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP).getNodeByPath(path);
            assignBusinessTermBTPage = editBIEPage.getBBIEPanel(bbieNodeForLoop).clickAssignBusinessTermButton();
        }

        //verify end user can see all random business terms assigned to the selected BIE
        WebElement bbieNodeForCheck = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP).getNodeByPath(path);
        BusinessTermAssignmentPage btAssignmentPageForSelectedBIE = editBIEPage.getBBIEPanel(bbieNodeForCheck).clickShowBusinessTermsButton();
        for (int i = 0; i < businessTerms.size(); i++) {
            btAssignmentPageForSelectedBIE.setBusinessTerm(businessTerms.get(i).getBusinessTerm());
            click(btAssignmentPageForSelectedBIE.getSearchButton());
            assertTrue(btAssignmentPageForSelectedBIE.getSelectCheckboxAtIndex(1).isDisplayed());
        }
    }

    @Test
    @DisplayName("TC_42_3_2")
    public void end_user_can_hover_over_show_business_terms_button_view_up_to_five_business_term_assigned_in_the_descendent_bie_panel() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        //use pre-existing BBIE node
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        //Assign business term to pre-existing, used BBIE node
        assertTrue(bbiePanel.getAssignBusinessTermButton(true).isEnabled());
        AssignBusinessTermBTPage assignBusinessTermBTPage = bbiePanel.clickAssignBusinessTermButton();
        //assign up to 7 random business terms to selected BIE for testing purpose
        ArrayList<BusinessTermObject> businessTerms = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTerms.add(randomBusinessTerm);
            assignBusinessTermBTPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
            assignBusinessTermBTPage.hitSearchButton();
            click(assignBusinessTermBTPage.getSelectCheckboxAtIndex(1));
            click(assignBusinessTermBTPage.getCreateButton());
            WebElement bbieNodeForLoop = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP).getNodeByPath(path);
            assignBusinessTermBTPage = editBIEPage.getBBIEPanel(bbieNodeForLoop).clickAssignBusinessTermButton();
        }
        //verify hovering display up to 5 business terms if assigned to selected BIE
    }

    @Test
    @DisplayName("TC_42_3_3")
    public void end_user_can_click_assign_business_term_button_in_descendent_bie_panel_assign_business_terms() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        //use pre-existing BBIE node
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        //Assign business term to pre-existing, used BBIE node
        assertTrue(bbiePanel.getAssignBusinessTermButton(true).isEnabled());
        AssignBusinessTermBTPage assignBusinessTermBTPage = bbiePanel.clickAssignBusinessTermButton();
        assignBusinessTermBTPage.create(randomBusinessTerm);

        WebElement bbieNodeForCheck = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP).getNodeByPath(path);
        BusinessTermAssignmentPage btAssignmentPageForSelectedBIE = editBIEPage.getBBIEPanel(bbieNodeForCheck).clickShowBusinessTermsButton();
        btAssignmentPageForSelectedBIE.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        btAssignmentPageForSelectedBIE.hitSearchButton();
        assertTrue(btAssignmentPageForSelectedBIE.getSelectCheckboxAtIndex(1).isDisplayed());
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
