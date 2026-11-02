package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.condition.DisabledIfBusinessTermProperty;
import org.oagi.score.e2e.impl.AuthenticatedApiClient;
import org.oagi.score.e2e.impl.page.bie.EditBIEPageImpl;
import org.oagi.score.e2e.impl.page.business_term.AssignBusinessTermBTPageImpl;
import org.oagi.score.e2e.impl.page.business_term.BusinessTermAssignmentPageImpl;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermBTPage;
import org.oagi.score.e2e.page.business_term.BusinessTermAssignmentPage;
import org.jooq.Condition;
import org.jooq.CloseableDSLContext;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;

/**
 * Superseded by Issue #1754.
 *
 * <p>These cases drove Business Term assignment through the standalone 'Business Term Assignment' /
 * 'Assign Business Term' pages (opened from a BIE node's detail pane via
 * {@code clickShowBusinessTermsButton()} / {@code clickAssignBusinessTermButton()}). #1754 moved
 * Business Term management into the BIE editor's in-place 'Business Terms' chip field, so those
 * buttons are no longer rendered when the chip field is shown. The in-editor coverage now lives in
 * {@link TC_42_5_InPlaceBusinessTermManagement}. This class is kept compiling (bodies intact) for
 * reference but is disabled.
 */
@Execution(ExecutionMode.SAME_THREAD)
@org.junit.jupiter.api.Disabled("#1754: standalone Business Term Assignment pages superseded by the in-editor chip flow — see TC_42_5")
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

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_11")
    public void end_user_discarding_an_assignment_from_the_detail_page_removes_the_assignment_not_the_catalog_term() {
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

        loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        createAssignedBusinessTermForBbie(randomBusinessTerm, topLevelASBIEP, path, endUser, null, false);

        BigInteger bbieBiztermId = findBbieBiztermId(randomBusinessTerm.getBusinessTermId());

        // Open the assignment detail page directly and discard the assignment.
        String detailUrl = getConfig().getBaseUrl().resolve(
                "/business_term_management/assign_business_term/details/" + bbieBiztermId
                        + "?type=BBIE&id=" + bbieBiztermId).toString();
        getDriver().get(detailUrl);
        click(elementToBeClickable(getDriver(),
                By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]")));
        click(elementToBeClickable(getDriver(),
                By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]")));
        assertEquals("Discarded", getSnackBarMessage(getDriver()));

        // #1752 - C1: the assignment row must be removed, while the catalog business term remains.
        assertEquals(0L, dsl().fetchCount(BBIE_BIZTERM, BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(ULong.valueOf(bbieBiztermId))));
        assertEquals(1L, dsl().fetchCount(BUSINESS_TERM, BUSINESS_TERM.BUSINESS_TERM_ID.eq(ULong.valueOf(randomBusinessTerm.getBusinessTermId()))));
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_12")
    public void end_user_lands_on_the_assignment_list_scoped_to_the_bie_after_creating_a_single_assignment() {
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

        assertTrue(bbiePanel.getAssignBusinessTermButton(true).isEnabled());
        AssignBusinessTermBTPage assignBusinessTermBTPage = bbiePanel.clickAssignBusinessTermButton();
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        assignBusinessTermBTPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        assignBusinessTermBTPage.hitSearchButton();
        click(assignBusinessTermBTPage.getSelectCheckboxAtIndex(1));
        click(assignBusinessTermBTPage.getCreateButton());

        // #1753 - M1 (#1374): after creating an assignment for a single BIE, the assignment list
        // is scoped to that BIE (the navigation carries the bieId query parameter).
        assertEquals("Created", getSnackBarMessage(getDriver()));
        new org.openqa.selenium.support.ui.WebDriverWait(getDriver(), java.time.Duration.ofSeconds(10L))
                .until(d -> d.getCurrentUrl().contains("bieId="));
        assertTrue(getDriver().getCurrentUrl().contains("bieId="),
                "Expected the assignment list to be scoped to the created BIE, but the URL was: "
                        + getDriver().getCurrentUrl());
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_13")
    public void setting_a_business_term_preferred_demotes_the_previous_preferred_on_the_same_node() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        BusinessTermObject firstPreferredTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        BusinessTermObject secondTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        // Two assignments on the SAME BBIE node: the first preferred, the second not. Set up
        // deterministically via the DB so the test isolates the behaviour under test (the demotion
        // performed by the update path), not the two-step assign wizard.
        createAssignedBusinessTermForBbieUsingDb(firstPreferredTerm, topLevelASBIEP, path, endUser, null, true);
        createAssignedBusinessTermForBbieUsingDb(secondTerm, topLevelASBIEP, path, endUser, null, false);

        BigInteger firstBiztermId = findBbieBiztermId(firstPreferredTerm.getBusinessTermId());
        BigInteger secondBiztermId = findBbieBiztermId(secondTerm.getBusinessTermId());

        // #1752 - M1: set the second assignment preferred via the assignment detail page. The fix must
        // demote the previously preferred sibling on the SAME node (one-preferred-per-node, #1381) —
        // before the fix the demotion used the assignment PK as a node id and demoted nothing.
        String detailUrl = getConfig().getBaseUrl()
                .resolve("/business_term_management/assign_business_term/details/" + secondBiztermId
                        + "?type=BBIE&id=" + secondBiztermId).toString();
        getDriver().get(detailUrl);
        click(elementToBeClickable(getDriver(), By.xpath("//mat-checkbox[contains(., \"Preferred Business Term\")]")));
        click(elementToBeClickable(getDriver(), By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        // confirm the overwrite of the previously preferred term
        click(elementToBeClickable(getDriver(), By.xpath("//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        assertEquals("Updated", getSnackBarMessage(getDriver()));

        assertEquals(1L, primaryIndicatorOf(secondBiztermId));
        assertEquals(0L, primaryIndicatorOf(firstBiztermId));
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_14")
    public void assigning_the_same_business_term_twice_does_not_create_a_duplicate() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ensureBbiePersisted(topLevelASBIEP, endUser, path);
        BigInteger bbieId = findBieId("bbie", topLevelASBIEP.getTopLevelAsbiepId(), path);

        AuthenticatedApiClient api = new AuthenticatedApiClient(getDriver(), getConfig().getBaseUrl());
        String assignBody = "{\"biesToAssign\":[{\"bieId\":" + bbieId + ",\"bieType\":\"BBIE\"}],"
                + "\"primaryIndicator\":false,\"typeCode\":\"Synonym\"}";
        // #1752 - M3: the asbie/bbie_bizterm assignment is find-or-create, so assigning the identical
        // (node, business term, type code) twice must not create a duplicate row.
        assertEquals(204, api.postJson("/api/business-terms/" + randomBusinessTerm.getBusinessTermId() + "/assign", assignBody).statusCode());
        assertEquals(204, api.postJson("/api/business-terms/" + randomBusinessTerm.getBusinessTermId() + "/assign", assignBody).statusCode());
        assertEquals(1L, dsl().fetchCount(BBIE_BIZTERM.join(BCC_BIZTERM).on(BBIE_BIZTERM.BCC_BIZTERM_ID.eq(BCC_BIZTERM.BCC_BIZTERM_ID)),
                BCC_BIZTERM.BUSINESS_TERM_ID.eq(ULong.valueOf(randomBusinessTerm.getBusinessTermId()))));
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_15")
    public void assigning_to_a_nonexistent_bie_returns_a_clean_400() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        AuthenticatedApiClient api = new AuthenticatedApiClient(getDriver(), getConfig().getBaseUrl());
        // #1752 - M8: a nonexistent BIE id resolves to no CC and must yield a clean 400, not an NPE/500.
        String assignBody = "{\"biesToAssign\":[{\"bieId\":999999999,\"bieType\":\"BBIE\"}],"
                + "\"primaryIndicator\":false,\"typeCode\":null}";
        AuthenticatedApiClient.ApiResponse resp =
                api.postJson("/api/business-terms/" + randomBusinessTerm.getBusinessTermId() + "/assign", assignBody);
        assertEquals(400, resp.statusCode());
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_16")
    public void batch_discard_rolls_back_when_one_term_is_in_use() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        BusinessTermObject unusedTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        BusinessTermObject usedTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        // Make usedTerm in-use by assigning it to a BBIE node.
        createAssignedBusinessTermForBbie(usedTerm, topLevelASBIEP, path, endUser, null, false);

        AuthenticatedApiClient api = new AuthenticatedApiClient(getDriver(), getConfig().getBaseUrl());
        String batchBody = "{\"businessTermIdList\":[" + unusedTerm.getBusinessTermId()
                + "," + usedTerm.getBusinessTermId() + "]}";
        // #1752 - M9: the batch discard runs in a single transaction, so a mid-batch in-use failure
        // rolls back the whole batch (neither term is deleted) and returns a clean 400.
        assertEquals(400, api.deleteJson("/api/business-terms", batchBody).statusCode());
        assertEquals(1L, dsl().fetchCount(BUSINESS_TERM, BUSINESS_TERM.BUSINESS_TERM_ID.eq(ULong.valueOf(unusedTerm.getBusinessTermId()))));
        assertEquals(1L, dsl().fetchCount(BUSINESS_TERM, BUSINESS_TERM.BUSINESS_TERM_ID.eq(ULong.valueOf(usedTerm.getBusinessTermId()))));
    }

    @Test
    @DisabledIfBusinessTermProperty(value = false)
    @DisplayName("TC_42_2_17")
    public void setting_preferred_with_a_different_type_code_warns_and_demotes_the_previous_preferred_on_the_same_node() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");
        String path = "/" + asccp.getPropertyTerm() + "/Note";
        BusinessTermObject firstPreferredTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        BusinessTermObject secondTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        // #1381: "preferred" is one-per-BIE-node; Type Code does NOT scope it. Seed two assignments on
        // the SAME BBIE node with DIFFERENT type codes: the first preferred, the second not. Promoting
        // the second to preferred must warn (a preferred already exists on the BIE) and demote the
        // first, even though the type codes differ. Before the fix the frontend "does a preferred
        // already exist?" check filtered by type code, so it fired no warning and the previous
        // preferred term (a different type code) was silently demoted.
        createAssignedBusinessTermForBbieUsingDb(firstPreferredTerm, topLevelASBIEP, path, endUser, "type code A", true);
        createAssignedBusinessTermForBbieUsingDb(secondTerm, topLevelASBIEP, path, endUser, "type code B", false);

        BigInteger firstBiztermId = findBbieBiztermId(firstPreferredTerm.getBusinessTermId());
        BigInteger secondBiztermId = findBbieBiztermId(secondTerm.getBusinessTermId());

        // Promote the second assignment to preferred via the assignment detail page.
        String detailUrl = getConfig().getBaseUrl()
                .resolve("/business_term_management/assign_business_term/details/" + secondBiztermId
                        + "?type=BBIE&id=" + secondBiztermId).toString();
        getDriver().get(detailUrl);
        click(elementToBeClickable(getDriver(), By.xpath("//mat-checkbox[contains(., \"Preferred Business Term\")]")));
        click(elementToBeClickable(getDriver(), By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        // #1381: the overwrite warning MUST fire even though the existing preferred term has a
        // different Type Code (the "preferred already exists?" check is node-scoped, matching the
        // node-scoped backend demotion).
        assertTrue(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//*[contains(text(), \"Overwrite previous preferred business term?\")]")).isDisplayed());
        // confirm the overwrite of the previously preferred term
        click(elementToBeClickable(getDriver(), By.xpath("//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        assertEquals("Updated", getSnackBarMessage(getDriver()));

        // The second (type code B) is now preferred; the first (type code A) has been demoted.
        assertEquals(1L, primaryIndicatorOf(secondBiztermId));
        assertEquals(0L, primaryIndicatorOf(firstBiztermId));
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        if (dsl != null) {
            dsl.close();
            dsl = null;
        }

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private CloseableDSLContext dsl;

    private DSLContext dsl() {
        if (dsl == null) {
            dsl = DSL.using(
                    getConfig().getProperty("org.oagi.score.e2e.datasource.url"),
                    getConfig().getProperty("org.oagi.score.e2e.datasource.username"),
                    getConfig().getProperty("org.oagi.score.e2e.datasource.password"));
        }
        return dsl;
    }

    private BigInteger findBbieBiztermId(BigInteger businessTermId) {
        ULong id = dsl().select(BBIE_BIZTERM.BBIE_BIZTERM_ID)
                .from(BBIE_BIZTERM)
                .join(BCC_BIZTERM).on(BBIE_BIZTERM.BCC_BIZTERM_ID.eq(BCC_BIZTERM.BCC_BIZTERM_ID))
                .where(BCC_BIZTERM.BUSINESS_TERM_ID.eq(ULong.valueOf(businessTermId)))
                .orderBy(BBIE_BIZTERM.BBIE_BIZTERM_ID.desc())
                .limit(1)
                .fetchOneInto(ULong.class);
        if (id == null) {
            throw new IllegalStateException("Unable to find bbie_bizterm id for business term " + businessTermId);
        }
        return id.toBigInteger();
    }

    private long primaryIndicatorOf(BigInteger bbieBiztermId) {
        Byte indicator = dsl().select(BBIE_BIZTERM.PRIMARY_INDICATOR)
                .from(BBIE_BIZTERM)
                .where(BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(ULong.valueOf(bbieBiztermId)))
                .fetchOneInto(Byte.class);
        return indicator == null ? 0L : indicator;
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
        BigInteger bccId = findRelatedBccId(bbieId);
        BigInteger bccBiztermId = findOrCreateBccBizterm(bccId, businessTerm.getBusinessTermId(), creator.getAppUserId());
        insertBbieBizterm(bbieId, bccBiztermId, creator.getAppUserId(), typeCode, primaryIndicator);
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

    /** The columns of asbie or bbie that the lookups below use. */
    private record BieTable(Table<?> table, TableField<?, ULong> id, TableField<?, ULong> owner,
                            TableField<?, String> path, TableField<?, String> hashPath) {
    }

    private static BieTable bieTable(String name) {
        return switch (name) {
            case "asbie" -> new BieTable(ASBIE, ASBIE.ASBIE_ID, ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ASBIE.PATH, ASBIE.HASH_PATH);
            case "bbie" -> new BieTable(BBIE, BBIE.BBIE_ID, BBIE.OWNER_TOP_LEVEL_ASBIEP_ID, BBIE.PATH, BBIE.HASH_PATH);
            default -> throw new IllegalArgumentException("Unknown BIE table: " + name);
        };
    }

    private boolean hasSpecificBieRow(String bieTable, BigInteger ownerTopLevelAsbiepId, String path) {
        BieTable t = bieTable(bieTable);
        return dsl().fetchExists(t.table(), t.owner().eq(ULong.valueOf(ownerTopLevelAsbiepId))
                .and(t.path().eq(path).or(t.hashPath().eq(ObjectHelper.sha256(path)))));
    }

    private BigInteger findBieId(String bieTable, BigInteger ownerTopLevelAsbiepId, String path) {
        BieTable t = bieTable(bieTable);
        ULong owner = ULong.valueOf(ownerTopLevelAsbiepId);
        ULong id = dsl().select(t.id()).from(t.table())
                .where(t.owner().eq(owner), t.path().eq(path).or(t.hashPath().eq(ObjectHelper.sha256(path))))
                .limit(1)
                .fetchOneInto(ULong.class);
        if (id != null) {
            return id.toBigInteger();
        }

        String suffix = path.startsWith("/") ? path.substring(1) : path;
        BigInteger fallback = findOnlyBieId(t, t.owner().eq(owner).and(t.path().like("%" + suffix)));
        if (fallback != null) {
            return fallback;
        }

        int idx = suffix.lastIndexOf('/');
        if (idx >= 0 && idx + 1 < suffix.length()) {
            fallback = findOnlyBieId(t, t.owner().eq(owner).and(t.path().like("%" + suffix.substring(idx + 1))));
            if (fallback != null) {
                return fallback;
            }
        }

        fallback = findOnlyBieId(t, t.owner().eq(owner));
        if (fallback != null) {
            return fallback;
        }
        throw new IllegalStateException("Unable to find " + bieTable + " for path " + path
                + ". Candidates: " + dumpBieRows(bieTable, ownerTopLevelAsbiepId));
    }

    /** The id of the only row that matches, or null when none or more than one match. */
    private BigInteger findOnlyBieId(BieTable t, Condition condition) {
        List<ULong> ids = dsl().selectDistinct(t.id()).from(t.table()).where(condition).fetchInto(ULong.class);
        return ids.size() == 1 ? ids.get(0).toBigInteger() : null;
    }

    private String dumpBieRows(String bieTable, BigInteger ownerTopLevelAsbiepId) {
        BieTable t = bieTable(bieTable);
        try {
            List<String> rows = new ArrayList<>();
            dsl().select(t.id(), t.path()).from(t.table())
                    .where(t.owner().eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                    .orderBy(t.path())
                    .forEach(r -> rows.add(r.value1().toBigInteger() + ":" + r.value2()));
            return rows.toString();
        } catch (RuntimeException e) {
            return "<failed to dump " + bieTable + " rows: " + e.getMessage() + ">";
        }
    }

    private BigInteger findAssignedAsbieId(BusinessTermObject businessTerm, AppUserObject creator) {
        ULong id = dsl().select(ASBIE_BIZTERM.ASBIE_ID)
                .from(ASBIE_BIZTERM)
                .join(ASCC_BIZTERM).on(ASBIE_BIZTERM.ASCC_BIZTERM_ID.eq(ASCC_BIZTERM.ASCC_BIZTERM_ID))
                .where(ASCC_BIZTERM.BUSINESS_TERM_ID.eq(ULong.valueOf(businessTerm.getBusinessTermId())))
                .orderBy(ASBIE_BIZTERM.ASBIE_BIZTERM_ID.desc())
                .limit(1)
                .fetchOneInto(ULong.class);
        if (id == null) {
            throw new IllegalStateException("Unable to find assigned ASBIE id for business term "
                    + businessTerm.getBusinessTerm());
        }
        return id.toBigInteger();
    }

    private BigInteger findAssignedBbieId(BusinessTermObject businessTerm, AppUserObject creator) {
        ULong id = dsl().select(BBIE_BIZTERM.BBIE_ID)
                .from(BBIE_BIZTERM)
                .join(BCC_BIZTERM).on(BBIE_BIZTERM.BCC_BIZTERM_ID.eq(BCC_BIZTERM.BCC_BIZTERM_ID))
                .where(BCC_BIZTERM.BUSINESS_TERM_ID.eq(ULong.valueOf(businessTerm.getBusinessTermId())))
                .orderBy(BBIE_BIZTERM.BBIE_BIZTERM_ID.desc())
                .limit(1)
                .fetchOneInto(ULong.class);
        if (id == null) {
            throw new IllegalStateException("Unable to find assigned BBIE id for business term "
                    + businessTerm.getBusinessTerm());
        }
        return id.toBigInteger();
    }

    private BigInteger findRelatedBccId(BigInteger bbieId) {
        ULong id = dsl().select(BCC_MANIFEST.BCC_ID)
                .from(BBIE)
                .join(BCC_MANIFEST).on(BBIE.BASED_BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID))
                .where(BBIE.BBIE_ID.eq(ULong.valueOf(bbieId)))
                .fetchOneInto(ULong.class);
        if (id == null) {
            throw new IllegalStateException("Unable to resolve related CC for bbie " + bbieId);
        }
        return id.toBigInteger();
    }

    private BigInteger findOrCreateBccBizterm(BigInteger bccId, BigInteger businessTermId, BigInteger userId) {
        ULong existing = dsl().select(BCC_BIZTERM.BCC_BIZTERM_ID)
                .from(BCC_BIZTERM)
                .where(BCC_BIZTERM.BUSINESS_TERM_ID.eq(ULong.valueOf(businessTermId)),
                        BCC_BIZTERM.BCC_ID.eq(ULong.valueOf(bccId)))
                .fetchAnyInto(ULong.class);
        if (existing != null) {
            return existing.toBigInteger();
        }

        LocalDateTime now = LocalDateTime.now();
        ULong id = dsl().insertInto(BCC_BIZTERM)
                .set(BCC_BIZTERM.BCC_ID, ULong.valueOf(bccId))
                .set(BCC_BIZTERM.BUSINESS_TERM_ID, ULong.valueOf(businessTermId))
                .set(BCC_BIZTERM.CREATED_BY, ULong.valueOf(userId))
                .set(BCC_BIZTERM.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(BCC_BIZTERM.CREATION_TIMESTAMP, now)
                .set(BCC_BIZTERM.LAST_UPDATE_TIMESTAMP, now)
                .returning(BCC_BIZTERM.BCC_BIZTERM_ID)
                .fetchOne(BCC_BIZTERM.BCC_BIZTERM_ID);
        if (id == null) {
            throw new IllegalStateException("Unable to create bcc_bizterm record.");
        }
        return id.toBigInteger();
    }

    private void insertBbieBizterm(BigInteger bbieId, BigInteger bccBiztermId, BigInteger userId,
                                   String typeCode, boolean primaryIndicator) {
        LocalDateTime now = LocalDateTime.now();
        dsl().insertInto(BBIE_BIZTERM)
                .set(BBIE_BIZTERM.BBIE_ID, ULong.valueOf(bbieId))
                .set(BBIE_BIZTERM.BCC_BIZTERM_ID, ULong.valueOf(bccBiztermId))
                .set(BBIE_BIZTERM.PRIMARY_INDICATOR, (byte) (primaryIndicator ? 1 : 0))
                .set(BBIE_BIZTERM.TYPE_CODE, typeCode)
                .set(BBIE_BIZTERM.CREATED_BY, ULong.valueOf(userId))
                .set(BBIE_BIZTERM.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(BBIE_BIZTERM.CREATION_TIMESTAMP, now)
                .set(BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP, now)
                .execute();
    }
}
