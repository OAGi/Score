package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.condition.DisabledIfBusinessTermProperty;
import org.oagi.score.e2e.impl.page.bie.EditBIEPageImpl;
import org.oagi.score.e2e.impl.page.business_term.AssignBusinessTermBTPageImpl;
import org.oagi.score.e2e.impl.page.business_term.BusinessTermAssignmentPageImpl;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermBTPage;
import org.oagi.score.e2e.page.business_term.BusinessTermAssignmentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_42_2_BusinessTermAssignment extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_1")
    public void end_user_opens_business_term_assignment_page_from_bie_detail() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        //Assign business term to pre-existing, used BBIE node
        assertTrue(bbiePanel.getAssignBusinessTermButton(true).isEnabled());
        BusinessTermAssignmentPage businessTermAssignmentPage = bbiePanel.clickShowBusinessTermsButton();
        assertEquals("Business Term Assignment", getText(businessTermAssignmentPage.getTitle()));
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_2")
    public void end_user_can_view_all_business_terms_with_assignments_on_business_term_assignment_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        //BBIE
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        //Assign business term to pre-existing, used BBIE node
        assertTrue(bbiePanel.getAssignBusinessTermButton(true).isEnabled());
        AssignBusinessTermBTPage assignBusinessTermBTPage = bbiePanel.clickAssignBusinessTermButton();
        //assign up to 3 random business terms to selected BIE for testing purpose
        List<BusinessTermObject> businessTerms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTerms.add(randomBusinessTerm);
            assignBusinessTermBTPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
            assignBusinessTermBTPage.hitSearchButton();
            click(assignBusinessTermBTPage.getSelectCheckboxAtIndex(1));
            click(assignBusinessTermBTPage.getCreateButton());
            WebElement bbieNodeForLoop = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP).getNodeByPath(path);
            assignBusinessTermBTPage = editBIEPage.getBBIEPanel(bbieNodeForLoop).clickAssignBusinessTermButton();
        }
        //ASBIE node
        ASCCPObject asccp2 = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP2 = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp2, endUser, "WIP");

        EditBIEPage editBIEPage2 = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP2);

        String path2 = "/" + asccp2.getPropertyTerm() + "/Data Area/Item Certificate Of Analysis";
        WebElement asbieNode = editBIEPage2.getNodeByPath(path2);
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage2.getASBIEPanel(asbieNode);

        asbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        //Assign business term to pre-existing, used ASBIE node
        assertTrue(asbiePanel.getAssignBusinessTermButton(true).isEnabled());
        AssignBusinessTermBTPage assignBusinessTermBTPageASBIE = asbiePanel.clickAssignBusinessTermButton();
        //assign up to 3 random business terms to selected BIE for testing purpose
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTerms.add(randomBusinessTerm);
            assignBusinessTermBTPageASBIE.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
            assignBusinessTermBTPageASBIE.hitSearchButton();
            click(assignBusinessTermBTPageASBIE.getSelectCheckboxAtIndex(1));
            click(assignBusinessTermBTPageASBIE.getCreateButton());
            WebElement asbieNodeForLoop = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP2).getNodeByPath(path2);
            assignBusinessTermBTPageASBIE = editBIEPage.getASBIEPanel(asbieNodeForLoop).clickAssignBusinessTermButton();
        }
        //verify all random business terms assigned to the selected BIE are displayed in Business Term Assignment page
        WebElement bbieNodeForCheck = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP).getNodeByPath(path);
        BusinessTermAssignmentPage btAssignmentPageForSelectedBIE = editBIEPage.getBBIEPanel(bbieNodeForCheck).clickShowBusinessTermsButton();
        btAssignmentPageForSelectedBIE.showAdvancedSearchPanel();
        assertTrue(btAssignmentPageForSelectedBIE.getTurnOffButton().isEnabled());
        click(btAssignmentPageForSelectedBIE.getTurnOffButton()); // list all BIEs in the business term assignment page
        for (int i = 0; i < businessTerms.size(); i++) {
            btAssignmentPageForSelectedBIE.setBusinessTerm(businessTerms.get(i).getBusinessTerm());
            click(btAssignmentPageForSelectedBIE.getSearchButton());
            assertTrue(btAssignmentPageForSelectedBIE.getSelectCheckboxAtIndex(1).isDisplayed());
        }
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_3")
    public void end_user_can_view_asbies_and_bbies_on_business_term_assignment_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        //BBIE
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        createAssignedBusinessTermForBbie(randomBusinessTerm, topLevelASBIEP, path, endUser, null, false);

        //ASBIE
        ASCCPObject asccp2 = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP2 = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp2, endUser, "WIP");
        String path2 = "/" + asccp2.getPropertyTerm() + "/Data Area/Item Certificate Of Analysis";
        BusinessTermObject randomBusinessTerm2 = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        createAssignedBusinessTermForAsbie(randomBusinessTerm2, topLevelASBIEP2, path2, endUser, null, false);

        // Verify BBIE and ASBIE entries are visible from the assignment page.
        BusinessTermAssignmentPage assignmentPage = openBusinessTermAssignmentPage();
        assignmentPage.showAdvancedSearchPanel();
        assignmentPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        click(assignmentPage.getSearchButton());
        WebElement trBBIE = assignmentPage.getTableRecordByValue(randomBusinessTerm.getBusinessTerm());
        assertEquals(randomBusinessTerm.getBusinessTerm(),
                getText(assignmentPage.getColumnByName(trBBIE, "businessTerm")));

        assignmentPage.openPage();
        assignmentPage.showAdvancedSearchPanel();
        assignmentPage.setBusinessTerm(randomBusinessTerm2.getBusinessTerm());
        click(assignmentPage.getSearchButton());
        WebElement trASBIE = assignmentPage.getTableRecordByValue(randomBusinessTerm2.getBusinessTerm());
        assertEquals(randomBusinessTerm2.getBusinessTerm(),
                getText(assignmentPage.getColumnByName(trASBIE, "businessTerm")));
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_4")
    public void end_user_can_search_business_term_assignments_by_supported_fields_on_business_term_assignment_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        //BBIE
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        createAssignedBusinessTermForBbie(randomBusinessTerm, topLevelASBIEP, path, endUser, null, false);

        //ASBIE
        ASCCPObject asccp2 = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP2 = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp2, endUser, "WIP");
        String path2 = "/" + asccp2.getPropertyTerm() + "/Data Area/Item Certificate Of Analysis";
        BusinessTermObject randomBusinessTerm2 = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        createAssignedBusinessTermForAsbie(randomBusinessTerm2, topLevelASBIEP2, path2, endUser, "random type code", false);

        BusinessTermAssignmentPage selectedASBIEAssignmentPage = openBusinessTermAssignmentPage();
        selectedASBIEAssignmentPage.showAdvancedSearchPanel();
        selectedASBIEAssignmentPage.setBusinessTerm(randomBusinessTerm2.getBusinessTerm());
        click(selectedASBIEAssignmentPage.getSearchButton());
        String assignedASBIEDen = getText(selectedASBIEAssignmentPage.getColumnByName(
                selectedASBIEAssignmentPage.getTableRecordByValue(randomBusinessTerm2.getBusinessTerm()), "bieDen"));

        // Search by BIE DEN, business term, external reference URI, and type code on assignment pages.
        BusinessTermAssignmentPage bbieAssignmentPage = openBusinessTermAssignmentPage();
        bbieAssignmentPage.showAdvancedSearchPanel();
        bbieAssignmentPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        click(bbieAssignmentPage.getSearchButton());
        WebElement trByBusinessTerm = bbieAssignmentPage.getTableRecordByValue(randomBusinessTerm.getBusinessTerm());
        assertEquals(randomBusinessTerm.getBusinessTerm(),
                getText(bbieAssignmentPage.getColumnByName(trByBusinessTerm, "businessTerm")));

        bbieAssignmentPage.openPage();
        bbieAssignmentPage.showAdvancedSearchPanel();
        bbieAssignmentPage.setExternalReferenceURI(randomBusinessTerm.getExternalReferenceUri());
        click(bbieAssignmentPage.getSearchButton());
        WebElement trByExternalReferenceURI = bbieAssignmentPage.getTableRecordByValue(randomBusinessTerm.getExternalReferenceUri());
        assertEquals(randomBusinessTerm.getExternalReferenceUri(),
                getText(bbieAssignmentPage.getColumnByName(trByExternalReferenceURI, "externalReferenceUri")));

        BusinessTermAssignmentPage asbieAssignmentPage = openBusinessTermAssignmentPage();
        asbieAssignmentPage.showAdvancedSearchPanel();
        asbieAssignmentPage.setBIEDenField(assignedASBIEDen);
        click(asbieAssignmentPage.getSearchButton());
        WebElement trByDen = asbieAssignmentPage.getTableRecordByValue(assignedASBIEDen);
        assertEquals(assignedASBIEDen, getText(asbieAssignmentPage.getColumnByName(trByDen, "bieDen")));
        assertEquals("ASBIE", getText(asbieAssignmentPage.getColumnByName(trByDen, "bieType")));

        asbieAssignmentPage.openPage();
        asbieAssignmentPage.showAdvancedSearchPanel();
        asbieAssignmentPage.setTypeCodeField("random type code");
        click(asbieAssignmentPage.getSearchButton());
        WebElement trByTypeCode = asbieAssignmentPage.getTableRecordByValue("random type code");
        assertEquals("random type code",
                getText(asbieAssignmentPage.getColumnByName(trByTypeCode, "typeCode")));
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_5")
    public void end_user_can_filter_preferred_business_terms_on_business_term_assignment_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        //BBIE
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        List<BusinessTermObject> businessTerms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTerms.add(randomBusinessTerm);
            createAssignedBusinessTermForBbie(randomBusinessTerm, topLevelASBIEP, path, endUser, null, i == 0);
        }

        String preferredBusinessTerm = businessTerms.get(0).getBusinessTerm();
        BusinessTermAssignmentPage btAssignmentPageForSelectedBIE =
                openSelectedBusinessTermAssignmentPage(homePage, topLevelASBIEP, path, false);
        btAssignmentPageForSelectedBIE.showAdvancedSearchPanel();
        assertTrue(btAssignmentPageForSelectedBIE.getTurnOffButton().isEnabled());
        click(btAssignmentPageForSelectedBIE.getPreferredOnlyCheckbox());
        click(btAssignmentPageForSelectedBIE.getSearchButton());
        assertEquals(preferredBusinessTerm,
                getText(btAssignmentPageForSelectedBIE.getColumnByName(
                        btAssignmentPageForSelectedBIE.getTableRecordAtIndex(1), "businessTerm")));
        assertEquals(0, getDriver().findElements(By.xpath(
                "//tbody//span[contains(text(), \"" + businessTerms.get(1).getBusinessTerm() + "\")]")).size());
        assertEquals(0, getDriver().findElements(By.xpath(
                "//tbody//span[contains(text(), \"" + businessTerms.get(2).getBusinessTerm() + "\")]")).size());
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_6")
    public void end_user_can_view_assignments_for_the_selected_bie_only_on_business_term_assignment_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        //BBIE
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        List<BusinessTermObject> businessTermsBBIE = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTermsBBIE.add(randomBusinessTerm);
            createAssignedBusinessTermForBbie(randomBusinessTerm, topLevelASBIEP, path, endUser, null, false);
        }

        ASCCPObject asccp2 = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP2 = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp2, endUser, "WIP");

        String path2 = "/" + asccp2.getPropertyTerm() + "/Data Area/Item Certificate Of Analysis";
        List<BusinessTermObject> businessTermsASBIE = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTermsASBIE.add(randomBusinessTerm);
            createAssignedBusinessTermForAsbie(randomBusinessTerm, topLevelASBIEP2, path2, endUser, null, false);
        }

        BusinessTermAssignmentPage btAssignmentPageForSelectedBIE =
                openSelectedBusinessTermAssignmentPage(homePage, topLevelASBIEP, path, false);
        btAssignmentPageForSelectedBIE.showAdvancedSearchPanel();
        assertTrue(btAssignmentPageForSelectedBIE.getTurnOffButton().isEnabled());
        for (BusinessTermObject businessTerm : businessTermsBBIE) {
            btAssignmentPageForSelectedBIE.getBusinessTermField().clear();
            btAssignmentPageForSelectedBIE.setBusinessTerm(businessTerm.getBusinessTerm());
            click(btAssignmentPageForSelectedBIE.getSearchButton());
            assertEquals(businessTerm.getBusinessTerm(),
                    getText(btAssignmentPageForSelectedBIE.getColumnByName(
                            btAssignmentPageForSelectedBIE.getTableRecordByValue(businessTerm.getBusinessTerm()),
                            "businessTerm")));
        }

        for (BusinessTermObject businessTerm : businessTermsASBIE) {
            String businessTermName = businessTerm.getBusinessTerm();
            btAssignmentPageForSelectedBIE.getBusinessTermField().clear();
            btAssignmentPageForSelectedBIE.setBusinessTerm(businessTermName);
            click(btAssignmentPageForSelectedBIE.getSearchButton());
            assertEquals(0, getDriver().findElements(By.xpath("//table//*[contains(text(), \"" + businessTermName + "\")]")).size());
        }
    }

    @Test
    @DisplayName("TC_42_2_7")
    public void end_user_can_view_all_available_business_terms_for_the_selected_bie_on_assign_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        //BBIE
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        List<BusinessTermObject> businessTermsBBIE = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTermsBBIE.add(randomBusinessTerm);
            createAssignedBusinessTermForBbie(randomBusinessTerm, topLevelASBIEP, path, endUser, null, false);
        }

        ASCCPObject asccp2 = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP2 = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp2, endUser, "WIP");

        String path2 = "/" + asccp2.getPropertyTerm() + "/Data Area/Item Certificate Of Analysis";
        List<BusinessTermObject> businessTermsASBIE = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTermsASBIE.add(randomBusinessTerm);
            createAssignedBusinessTermForAsbie(randomBusinessTerm, topLevelASBIEP2, path2, endUser, null, false);
        }

        AssignBusinessTermBTPage assignBTPageForSelectedBIE =
                openSelectedAssignBusinessTermPage(homePage, topLevelASBIEP, path, false);
        for (BusinessTermObject businessTerm : businessTermsBBIE) {
            assignBTPageForSelectedBIE.getBusinessTermField().clear();
            assignBTPageForSelectedBIE.setBusinessTerm(businessTerm.getBusinessTerm());
            click(assignBTPageForSelectedBIE.getSearchButton());
            assertTrue(assignBTPageForSelectedBIE.getSelectCheckboxAtIndex(1).isDisplayed());
        }

        for (BusinessTermObject businessTerm : businessTermsASBIE) {
            assignBTPageForSelectedBIE.getBusinessTermField().clear();
            assignBTPageForSelectedBIE.setBusinessTerm(businessTerm.getBusinessTerm());
            click(assignBTPageForSelectedBIE.getSearchButton());
            assertTrue(assignBTPageForSelectedBIE.getSelectCheckboxAtIndex(1).isDisplayed());
        }
    }

    @Test
    @DisplayName("TC_42_2_8")
    public void end_user_can_filter_business_terms_assigned_to_the_same_core_component_on_assign_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        //BBIE
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        List<BusinessTermObject> businessTermsBBIE = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTermsBBIE.add(randomBusinessTerm);
            createAssignedBusinessTermForBbie(randomBusinessTerm, topLevelASBIEP, path, endUser, null, false);
        }

        ASCCPObject asccp2 = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP2 = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp2, endUser, "WIP");

        String path2 = "/" + asccp2.getPropertyTerm() + "/Data Area/Item Certificate Of Analysis";
        List<BusinessTermObject> businessTermsASBIE = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
            businessTermsASBIE.add(randomBusinessTerm);
            createAssignedBusinessTermForAsbie(randomBusinessTerm, topLevelASBIEP2, path2, endUser, null, false);
        }

        AssignBusinessTermBTPage assignBTPageForSelectedBIE =
                openSelectedAssignBusinessTermPage(homePage, topLevelASBIEP, path, false);
        assignBTPageForSelectedBIE.showAdvancedSearchPanel();
        click(assignBTPageForSelectedBIE.getFilterBySameCCCheckbox());
        click(assignBTPageForSelectedBIE.getSearchButton());
        for (BusinessTermObject businessTerm : businessTermsBBIE) {
            assignBTPageForSelectedBIE.getBusinessTermField().clear();
            assignBTPageForSelectedBIE.setBusinessTerm(businessTerm.getBusinessTerm());
            click(assignBTPageForSelectedBIE.getSearchButton());
            assertTrue(assignBTPageForSelectedBIE.getSelectCheckboxAtIndex(1).isDisplayed());
        }

        for (BusinessTermObject businessTerm : businessTermsASBIE) {
            String businessTermName = businessTerm.getBusinessTerm();
            assignBTPageForSelectedBIE.getBusinessTermField().clear();
            assignBTPageForSelectedBIE.setBusinessTerm(businessTermName);
            click(assignBTPageForSelectedBIE.getSearchButton());
            assertEquals(0, getDriver().findElements(By.xpath("//table//*[contains(text(), \"" + businessTermName + "\")]")).size());
        }
    }

    @Test
    @DisplayName("TC_42_2_9")
    public void end_user_gets_expected_duplicate_rules_for_business_term_and_type_code_on_assign_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        //ASBIE node
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, endUser, "WIP");

        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Data Area/Item Certificate Of Analysis";
        WebElement asbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);

        asbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        // Assign business terms to the same selected BIE and verify the duplicate rules.
        assertTrue(asbiePanel.getAssignBusinessTermButton(true).isEnabled());
        AssignBusinessTermBTPage assignBusinessTermBTPageASBIE = asbiePanel.clickAssignBusinessTermButton();
        BusinessTermObject businessTerm1 = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        BusinessTermObject businessTerm2 = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        assignBusinessTermBTPageASBIE.setBusinessTerm(businessTerm1.getBusinessTerm());
        assignBusinessTermBTPageASBIE.hitSearchButton();
        click(assignBusinessTermBTPageASBIE.getSelectCheckboxAtIndex(1));
        assignBusinessTermBTPageASBIE.setTypeCode("type code 1");
        click(assignBusinessTermBTPageASBIE.getCreateButton());

        editBIEPage.openPage();
        WebElement asbieNodeForSameTypeCheck = editBIEPage.getNodeByPath(path);
        assignBusinessTermBTPageASBIE = editBIEPage.getASBIEPanel(asbieNodeForSameTypeCheck).clickAssignBusinessTermButton();
        assignBusinessTermBTPageASBIE.setBusinessTerm(businessTerm1.getBusinessTerm());
        assignBusinessTermBTPageASBIE.hitSearchButton();
        click(assignBusinessTermBTPageASBIE.getSelectCheckboxAtIndex(1));
        assignBusinessTermBTPageASBIE.setTypeCode("type code 1");
        click(assignBusinessTermBTPageASBIE.getCreateButton());
        assertTrue(getDriver().findElement(By.xpath(
                "//*[contains(text(), \"Another business term assignment for the same BIE and type code already exists!\")]"))
                .isDisplayed());

        editBIEPage.openPage();
        WebElement asbieNodeForDifferentTypeCheck = editBIEPage.getNodeByPath(path);
        assignBusinessTermBTPageASBIE = editBIEPage.getASBIEPanel(asbieNodeForDifferentTypeCheck).clickAssignBusinessTermButton();
        assignBusinessTermBTPageASBIE.setBusinessTerm(businessTerm1.getBusinessTerm());
        assignBusinessTermBTPageASBIE.hitSearchButton();
        click(assignBusinessTermBTPageASBIE.getSelectCheckboxAtIndex(1));
        assignBusinessTermBTPageASBIE.setTypeCode("type code 2");
        click(assignBusinessTermBTPageASBIE.getCreateButton());

        editBIEPage.openPage();
        WebElement asbieNodeForDifferentBusinessTermSameType = editBIEPage.getNodeByPath(path);
        assignBusinessTermBTPageASBIE = editBIEPage.getASBIEPanel(asbieNodeForDifferentBusinessTermSameType).clickAssignBusinessTermButton();
        assignBusinessTermBTPageASBIE.setBusinessTerm(businessTerm2.getBusinessTerm());
        assignBusinessTermBTPageASBIE.hitSearchButton();
        click(assignBusinessTermBTPageASBIE.getSelectCheckboxAtIndex(1));
        assignBusinessTermBTPageASBIE.setTypeCode("type code 1");
        click(assignBusinessTermBTPageASBIE.getCreateButton());

        editBIEPage.openPage();
        WebElement asbieNodeForDifferentBusinessTermDifferentType = editBIEPage.getNodeByPath(path);
        assignBusinessTermBTPageASBIE = editBIEPage.getASBIEPanel(asbieNodeForDifferentBusinessTermDifferentType).clickAssignBusinessTermButton();
        assignBusinessTermBTPageASBIE.setBusinessTerm(businessTerm2.getBusinessTerm());
        assignBusinessTermBTPageASBIE.hitSearchButton();
        click(assignBusinessTermBTPageASBIE.getSelectCheckboxAtIndex(1));
        assignBusinessTermBTPageASBIE.setTypeCode("type code 2");
        click(assignBusinessTermBTPageASBIE.getCreateButton());

        editBIEPage.openPage();
        WebElement asbieNodeForCheck = editBIEPage.getNodeByPath(path);
        BusinessTermAssignmentPage businessTermAssignmentPageForSelectBIE = editBIEPage.getASBIEPanel(asbieNodeForCheck).clickShowBusinessTermsButton();
        businessTermAssignmentPageForSelectBIE.showAdvancedSearchPanel();
        assertTrue(businessTermAssignmentPageForSelectBIE.getTurnOffButton().isEnabled());

        businessTermAssignmentPageForSelectBIE.setBusinessTerm(businessTerm1.getBusinessTerm());
        businessTermAssignmentPageForSelectBIE.setTypeCodeField("type code 1");
        click(businessTermAssignmentPageForSelectBIE.getSearchButton());
        assertTrue(businessTermAssignmentPageForSelectBIE.getSelectCheckboxAtIndex(1).isDisplayed());

        businessTermAssignmentPageForSelectBIE.openPage();
        businessTermAssignmentPageForSelectBIE.showAdvancedSearchPanel();
        businessTermAssignmentPageForSelectBIE.getTypeCodeField().clear();
        businessTermAssignmentPageForSelectBIE.setBusinessTerm(businessTerm1.getBusinessTerm());
        businessTermAssignmentPageForSelectBIE.setTypeCodeField("type code 2");
        click(businessTermAssignmentPageForSelectBIE.getSearchButton());
        assertTrue(businessTermAssignmentPageForSelectBIE.getSelectCheckboxAtIndex(1).isDisplayed());

        businessTermAssignmentPageForSelectBIE.openPage();
        businessTermAssignmentPageForSelectBIE.showAdvancedSearchPanel();
        businessTermAssignmentPageForSelectBIE.setBusinessTerm(businessTerm2.getBusinessTerm());
        businessTermAssignmentPageForSelectBIE.setTypeCodeField("type code 1");
        click(businessTermAssignmentPageForSelectBIE.getSearchButton());
        assertTrue(businessTermAssignmentPageForSelectBIE.getSelectCheckboxAtIndex(1).isDisplayed());

        businessTermAssignmentPageForSelectBIE.openPage();
        businessTermAssignmentPageForSelectBIE.showAdvancedSearchPanel();
        businessTermAssignmentPageForSelectBIE.setBusinessTerm(businessTerm2.getBusinessTerm());
        businessTermAssignmentPageForSelectBIE.setTypeCodeField("type code 2");
        click(businessTermAssignmentPageForSelectBIE.getSearchButton());
        assertTrue(businessTermAssignmentPageForSelectBIE.getSelectCheckboxAtIndex(1).isDisplayed());
    }

    @Test
    @DisplayName("TC_42_2_10")
    public void end_user_gets_overwrite_warning_when_setting_another_preferred_business_term_for_the_same_bie() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        //BBIE
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        BusinessTermObject preferredBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        createAssignedBusinessTermForBbie(preferredBusinessTerm, topLevelASBIEP, path, endUser, null, true);

        BusinessTermObject anotherBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        AssignBusinessTermBTPage assignBusinessTermBTPage =
                openSelectedAssignBusinessTermPage(homePage, topLevelASBIEP, path, false);
        assignBusinessTermBTPage.setBusinessTerm(anotherBusinessTerm.getBusinessTerm());
        assignBusinessTermBTPage.hitSearchButton();
        click(assignBusinessTermBTPage.getSelectCheckboxAtIndex(1));
        click(assignBusinessTermBTPage.getPreferredBusinessTermCheckbox());
        click(assignBusinessTermBTPage.getCreateButton());
        assertTrue(getDriver().findElement(By.xpath(
                "//*[contains(text(), \"Overwrite previous preferred business terms?\")]")).isDisplayed());
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private BusinessTermAssignmentPage openBusinessTermAssignmentPage(String... bieTypes) {
        BusinessTermAssignmentPage businessTermAssignmentPage =
                new BusinessTermAssignmentPageImpl(getDriver(), getConfig(), getAPIFactory(),
                        Arrays.asList(bieTypes), null);
        businessTermAssignmentPage.openPage();
        if (bieTypes.length == 1) {
            businessTermAssignmentPage.showAdvancedSearchPanel();
            businessTermAssignmentPage.setType(bieTypes[0]);
            click(businessTermAssignmentPage.getSearchButton());
        }
        return businessTermAssignmentPage;
    }

    private BusinessTermAssignmentPage openBusinessTermAssignmentPage(String bieType, BigInteger bieId) {
        BusinessTermAssignmentPage businessTermAssignmentPage =
                new BusinessTermAssignmentPageImpl(getDriver(), getConfig(), getAPIFactory(),
                        Arrays.asList(bieType), bieId);
        businessTermAssignmentPage.openPage();
        return businessTermAssignmentPage;
    }

    private BusinessTermAssignmentPage openSelectedBusinessTermAssignmentPage(HomePage homePage,
                                                                              TopLevelASBIEPObject topLevelASBIEP,
                                                                              String path,
                                                                              boolean asbie) {
        String bieType = asbie ? "ASBIE" : "BBIE";
        BigInteger bieId = findBieId(bieType.toLowerCase(), topLevelASBIEP.getTopLevelAsbiepId(), path);
        BusinessTermAssignmentPage businessTermAssignmentPage =
                new BusinessTermAssignmentPageImpl(
                        new org.oagi.score.e2e.impl.page.LoginPageImpl(getDriver(), getConfig(), getAPIFactory()),
                        Arrays.asList(bieType), bieId);
        businessTermAssignmentPage.openPage();
        return businessTermAssignmentPage;
    }

    private AssignBusinessTermBTPage openSelectedAssignBusinessTermPage(HomePage homePage,
                                                                        TopLevelASBIEPObject topLevelASBIEP,
                                                                        String path,
                                                                        boolean asbie) {
        String bieType = asbie ? "ASBIE" : "BBIE";
        BigInteger bieId = findBieId(bieType.toLowerCase(), topLevelASBIEP.getTopLevelAsbiepId(), path);
        AssignBusinessTermBTPage assignBusinessTermBTPage =
                new AssignBusinessTermBTPageImpl(
                        new org.oagi.score.e2e.impl.page.LoginPageImpl(getDriver(), getConfig(), getAPIFactory()),
                        Arrays.asList(bieType), bieId);
        assignBusinessTermBTPage.openPage();
        return assignBusinessTermBTPage;
    }

    private void ensureUsed(EditBIEPage editBIEPage, EditBIEPage.ASBIEPanel asbiePanel) {
        if (!isMaterialCheckboxSelected(asbiePanel.getUsedCheckbox())) {
            asbiePanel.toggleUsed();
            click(editBIEPage.getUpdateButton(true));
        }
    }

    private void createAssignedBusinessTermForAsbie(BusinessTermObject businessTerm,
                                                    TopLevelASBIEPObject topLevelASBIEP,
                                                    String path,
                                                    AppUserObject creator,
                                                    String typeCode,
                                                    boolean primaryIndicator) {
        ensureAsbiePersisted(topLevelASBIEP, creator, path);

        EditBIEPage editBIEPage = new EditBIEPageImpl(
                new org.oagi.score.e2e.impl.page.LoginPageImpl(getDriver(), getConfig(), getAPIFactory()),
                topLevelASBIEP);
        editBIEPage.openPage();
        WebElement asbieNode = editBIEPage.getNodeByPath(path, 3);
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        AssignBusinessTermBTPage assignBusinessTermBTPage = asbiePanel.clickAssignBusinessTermButton();
        assignBusinessTermBTPage.setBusinessTerm(businessTerm.getBusinessTerm());
        assignBusinessTermBTPage.hitSearchButton();
        click(assignBusinessTermBTPage.getSelectCheckboxAtIndex(1));
        if (typeCode != null) {
            assignBusinessTermBTPage.setTypeCode(typeCode);
        }
        if (primaryIndicator && !assignBusinessTermBTPage.getPreferredBusinessTermCheckbox().isSelected()) {
            click(assignBusinessTermBTPage.getPreferredBusinessTermCheckbox());
        }
        click(assignBusinessTermBTPage.getCreateButton());
    }

    private void createAssignedBusinessTermForBbie(BusinessTermObject businessTerm,
                                                   TopLevelASBIEPObject topLevelASBIEP,
                                                   String path,
                                                   AppUserObject creator,
                                                   String typeCode,
                                                   boolean primaryIndicator) {
        ensureBbiePersisted(topLevelASBIEP, creator, path);
        BigInteger bbieId = findBieId("bbie", topLevelASBIEP.getTopLevelAsbiepId(), path);
        AssignBusinessTermBTPage assignBusinessTermBTPage = new AssignBusinessTermBTPageImpl(
                new org.oagi.score.e2e.impl.page.LoginPageImpl(getDriver(), getConfig(), getAPIFactory()),
                Arrays.asList("BBIE"), bbieId);
        assignBusinessTermBTPage.openPage();
        assignBusinessTermBTPage.setBusinessTerm(businessTerm.getBusinessTerm());
        assignBusinessTermBTPage.hitSearchButton();
        click(assignBusinessTermBTPage.getSelectCheckboxAtIndex(1));
        if (typeCode != null) {
            assignBusinessTermBTPage.setTypeCode(typeCode);
        }
        if (primaryIndicator && !assignBusinessTermBTPage.getPreferredBusinessTermCheckbox().isSelected()) {
            click(assignBusinessTermBTPage.getPreferredBusinessTermCheckbox());
        }
        click(assignBusinessTermBTPage.getCreateButton());
    }

    private void createAssignedBusinessTermForBbieUsingDb(BusinessTermObject businessTerm,
                                                          TopLevelASBIEPObject topLevelASBIEP,
                                                          String path,
                                                          AppUserObject creator,
                                                          String typeCode,
                                                          boolean primaryIndicator) {
        ensureBbiePersisted(topLevelASBIEP, creator, path);
        BigInteger bbieId = findBieId("bbie", topLevelASBIEP.getTopLevelAsbiepId(), path);
        BigInteger bccId = findRelatedCcId("bbie", "bcc", "based_bcc_manifest_id", "bcc_manifest_id", bbieId);
        BigInteger bccBiztermId = findOrCreateCcBizterm("bcc_bizterm", "bcc_bizterm_id", "bcc_id",
                bccId, businessTerm.getBusinessTermId(), creator.getAppUserId());
        insertBieBizterm("bbie_bizterm", "bbie_bizterm_id", "bbie_id", "bcc_bizterm_id",
                bbieId, bccBiztermId, creator.getAppUserId(), typeCode, primaryIndicator);
    }

    private void ensureAsbiePersisted(TopLevelASBIEPObject topLevelASBIEP,
                                      AppUserObject owner,
                                      String path) {
        if (hasSpecificBieRow("asbie", topLevelASBIEP.getTopLevelAsbiepId(), path)) {
            return;
        }

        EditBIEPage editBIEPage = new EditBIEPageImpl(
                new org.oagi.score.e2e.impl.page.LoginPageImpl(getDriver(), getConfig(), getAPIFactory()),
                topLevelASBIEP);
        editBIEPage.openPage();
        WebElement node = editBIEPage.getNodeByPath(path, 3);
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(node);
        ensureUsed(editBIEPage, asbiePanel);
    }

    private void ensureBbiePersisted(TopLevelASBIEPObject topLevelASBIEP,
                                     AppUserObject owner,
                                     String path) {
        if (hasSpecificBieRow("bbie", topLevelASBIEP.getTopLevelAsbiepId(), path)) {
            return;
        }

        EditBIEPage editBIEPage = new EditBIEPageImpl(
                new org.oagi.score.e2e.impl.page.LoginPageImpl(getDriver(), getConfig(), getAPIFactory()),
                topLevelASBIEP);
        editBIEPage.openPage();
        WebElement node = editBIEPage.getNodeByPath(path, 3);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        if (!isMaterialCheckboxSelected(bbiePanel.getUsedCheckbox())) {
            bbiePanel.toggleUsed();
            click(editBIEPage.getUpdateButton(true));
        }
    }

    private boolean isMaterialCheckboxSelected(WebElement checkbox) {
        String cssClass = checkbox.getAttribute("class");
        return cssClass != null && cssClass.contains("mat-mdc-checkbox-checked");
    }

    private boolean hasSpecificBieRow(String bieTable, BigInteger ownerTopLevelAsbiepId, String path) {
        String sql = "select count(*) from " + bieTable +
                " where owner_top_level_asbiep_id = ? and (path = ? or hash_path = ?)";
        try (Connection connection = newConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerTopLevelAsbiepId.longValueExact());
            ps.setString(2, path);
            ps.setString(3, ObjectHelper.sha256(path));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to determine whether the target " + bieTable + " row exists.", e);
        }
    }

    private BigInteger findBieId(String bieTable, BigInteger ownerTopLevelAsbiepId, String path) {
        String sql = "select " + bieTable + "_id from " + bieTable +
                " where owner_top_level_asbiep_id = ? and (path = ? or hash_path = ?)";
        try (Connection connection = newConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerTopLevelAsbiepId.longValueExact());
            ps.setString(2, path);
            ps.setString(3, ObjectHelper.sha256(path));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1).toBigInteger();
                }
            }

            String suffix = path.startsWith("/") ? path.substring(1) : path;
            BigInteger fallback = findBieIdByPathSuffix(connection, bieTable, ownerTopLevelAsbiepId, suffix);
            if (fallback != null) {
                return fallback;
            }

            int idx = suffix.lastIndexOf('/');
            if (idx >= 0 && idx + 1 < suffix.length()) {
                fallback = findBieIdByPathSuffix(connection, bieTable, ownerTopLevelAsbiepId, suffix.substring(idx + 1));
                if (fallback != null) {
                    return fallback;
                }
            }

            fallback = findOnlyBieId(connection, bieTable, ownerTopLevelAsbiepId);
            if (fallback != null) {
                return fallback;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find " + bieTable + " by path " + path, e);
        }
        throw new IllegalStateException("Unable to find " + bieTable + " for path " + path
                + ". Candidates: " + dumpBieRows(bieTable, ownerTopLevelAsbiepId));
    }

    private BigInteger findBieIdByPathSuffix(Connection connection, String bieTable,
                                             BigInteger ownerTopLevelAsbiepId, String pathSuffix) throws SQLException {
        String sql = "select " + bieTable + "_id from " + bieTable +
                " where owner_top_level_asbiep_id = ? and path like ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerTopLevelAsbiepId.longValueExact());
            ps.setString(2, "%" + pathSuffix);
            try (ResultSet rs = ps.executeQuery()) {
                BigInteger result = null;
                while (rs.next()) {
                    BigInteger current = rs.getBigDecimal(1).toBigInteger();
                    if (result != null && !result.equals(current)) {
                        return null;
                    }
                    result = current;
                }
                return result;
            }
        }
    }

    private BigInteger findOnlyBieId(Connection connection, String bieTable,
                                     BigInteger ownerTopLevelAsbiepId) throws SQLException {
        String sql = "select " + bieTable + "_id from " + bieTable +
                " where owner_top_level_asbiep_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerTopLevelAsbiepId.longValueExact());
            try (ResultSet rs = ps.executeQuery()) {
                BigInteger result = null;
                while (rs.next()) {
                    BigInteger current = rs.getBigDecimal(1).toBigInteger();
                    if (result != null && !result.equals(current)) {
                        return null;
                    }
                    result = current;
                }
                return result;
            }
        }
    }

    private String dumpBieRows(String bieTable, BigInteger ownerTopLevelAsbiepId) {
        String sql = "select " + bieTable + "_id, path, hash_path from " + bieTable +
                " where owner_top_level_asbiep_id = ? order by path";
        try (Connection connection = newConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerTopLevelAsbiepId.longValueExact());
            try (ResultSet rs = ps.executeQuery()) {
                List<String> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(rs.getBigDecimal(1).toBigInteger() + ":" + rs.getString(2));
                }
                return rows.toString();
            }
        } catch (SQLException e) {
            return "<failed to dump " + bieTable + " rows: " + e.getMessage() + ">";
        }
    }

    private BigInteger findAssignedAsbieId(BusinessTermObject businessTerm, AppUserObject creator) {
        String sql = "select abt.asbie_id from asbie_bizterm abt " +
                "join ascc_bizterm acbt on abt.ascc_bizterm_id = acbt.ascc_bizterm_id " +
                "where acbt.business_term_id = ? " +
                "order by abt.asbie_bizterm_id desc limit 1";
        try (Connection connection = newConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, businessTerm.getBusinessTermId().longValueExact());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1).toBigInteger();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find assigned ASBIE id for business term "
                    + businessTerm.getBusinessTerm(), e);
        }
        throw new IllegalStateException("Unable to find assigned ASBIE id for business term "
                + businessTerm.getBusinessTerm());
    }

    private BigInteger findAssignedBbieId(BusinessTermObject businessTerm, AppUserObject creator) {
        String sql = "select bbt.bbie_id from bbie_bizterm bbt " +
                "join bcc_bizterm bcbt on bbt.bcc_bizterm_id = bcbt.bcc_bizterm_id " +
                "where bcbt.business_term_id = ? " +
                "order by bbt.bbie_bizterm_id desc limit 1";
        try (Connection connection = newConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, businessTerm.getBusinessTermId().longValueExact());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1).toBigInteger();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find assigned BBIE id for business term "
                    + businessTerm.getBusinessTerm(), e);
        }
        throw new IllegalStateException("Unable to find assigned BBIE id for business term "
                + businessTerm.getBusinessTerm());
    }

    private BigInteger findRelatedCcId(String bieTable, String ccTable, String manifestFkColumn,
                                       String manifestPkColumn, BigInteger bieId) {
        String sql = "select " + ccTable + "_id from " + bieTable + " b join " + ccTable + "_manifest m" +
                " on b." + manifestFkColumn + " = m." + manifestPkColumn +
                " where b." + bieTable + "_id = ?";
        try (Connection connection = newConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, bieId.longValueExact());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1).toBigInteger();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to resolve related CC for " + bieTable + " " + bieId, e);
        }
        throw new IllegalStateException("Unable to resolve related CC for " + bieTable + " " + bieId);
    }

    private BigInteger findOrCreateCcBizterm(String tableName, String pkColumn, String ccIdColumn,
                                             BigInteger ccId, BigInteger businessTermId, BigInteger userId) {
        String selectSql = "select " + pkColumn + " from " + tableName + " where business_term_id = ? and " + ccIdColumn + " = ?";
        try (Connection connection = newConnection();
             PreparedStatement selectPs = connection.prepareStatement(selectSql)) {
            selectPs.setLong(1, businessTermId.longValueExact());
            selectPs.setLong(2, ccId.longValueExact());
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1).toBigInteger();
                }
            }

            String insertSql = "insert into " + tableName + " (" + ccIdColumn + ", business_term_id, created_by, last_updated_by, creation_timestamp, last_update_timestamp)" +
                    " values (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertPs = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                insertPs.setLong(1, ccId.longValueExact());
                insertPs.setLong(2, businessTermId.longValueExact());
                insertPs.setLong(3, userId.longValueExact());
                insertPs.setLong(4, userId.longValueExact());
                insertPs.setTimestamp(5, now);
                insertPs.setTimestamp(6, now);
                insertPs.executeUpdate();
                try (ResultSet keys = insertPs.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getBigDecimal(1).toBigInteger();
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create " + tableName + " record.", e);
        }
        throw new IllegalStateException("Unable to create " + tableName + " record.");
    }

    private void insertBieBizterm(String tableName, String pkColumn, String bieIdColumn, String ccBiztermIdColumn,
                                  BigInteger bieId, BigInteger ccBiztermId, BigInteger userId,
                                  String typeCode, boolean primaryIndicator) {
        String insertSql = "insert into " + tableName + " (" + bieIdColumn + ", " + ccBiztermIdColumn +
                ", primary_indicator, type_code, created_by, last_updated_by, creation_timestamp, last_update_timestamp)" +
                " values (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = newConnection();
             PreparedStatement ps = connection.prepareStatement(insertSql)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setLong(1, bieId.longValueExact());
            ps.setLong(2, ccBiztermId.longValueExact());
            ps.setByte(3, (byte) (primaryIndicator ? 1 : 0));
            ps.setString(4, typeCode);
            ps.setLong(5, userId.longValueExact());
            ps.setLong(6, userId.longValueExact());
            ps.setTimestamp(7, now);
            ps.setTimestamp(8, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create " + tableName + " record for BIE " + bieId, e);
        }
    }

    private Connection newConnection() throws SQLException {
        return DriverManager.getConnection(
                getConfig().getProperty("org.oagi.score.e2e.datasource.url"),
                getConfig().getProperty("org.oagi.score.e2e.datasource.username"),
                getConfig().getProperty("org.oagi.score.e2e.datasource.password"));
    }
}
