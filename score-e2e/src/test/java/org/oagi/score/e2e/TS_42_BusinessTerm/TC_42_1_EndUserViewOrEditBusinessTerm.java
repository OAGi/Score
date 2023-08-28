package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.business_term.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_42_1_EndUserViewOrEditBusinessTerm extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_42_1_1")
    public void enduser_should_open_page_titled_business_term_under_bie_menu() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        String viewEditBusinessTermPageTitle = getText(bieMenu.openViewEditBusinessTermSubMenu().getTitle());
        assertEquals("Business Term", viewEditBusinessTermPageTitle);
    }

    @Test
    @DisplayName("TC_42_1_2")
    public void enduser_can_create_business_term_with_only_required_fields() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        CreateBusinessTermPage createBusinessTermPage = viewEditBusinessTermPage.openCreateBusinessTermPage();

        BusinessTermObject businessTerm = new BusinessTermObject();
        businessTerm.setBusinessTerm("bt_" + randomAlphanumeric(5, 10));
        businessTerm.setExternalReferenceUri("http://www." + randomAscii(3, 8) + ".com");
        viewEditBusinessTermPage = createBusinessTermPage.createBusinessTerm(businessTerm);
        EditBusinessTermPage editBusinessTermPage = viewEditBusinessTermPage.openEditBusinessTermPageByTerm(businessTerm.getBusinessTerm());
        assertEquals(businessTerm.getBusinessTerm(), editBusinessTermPage.getBusinessTermFieldText());
        assertTrue(StringUtils.isEmpty(editBusinessTermPage.getDefinitionFieldText()));
    }

    @Test
    @DisplayName("TC_42_1_3")
    public void enduser_cannot_create_business_term_if_any_required_field_missing() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        CreateBusinessTermPage createBusinessTermPage = viewEditBusinessTermPage.openCreateBusinessTermPage();

        BusinessTermObject businessTerm1 = new BusinessTermObject();
        businessTerm1.setBusinessTerm("bt_" + randomAlphanumeric(5, 10));
        assertThrows(TimeoutException.class, () -> createBusinessTermPage.createBusinessTerm(businessTerm1));

        BusinessTermObject businessTerm2 = new BusinessTermObject();
        businessTerm2.setExternalReferenceUri("http://www." + randomAscii(3, 8) + ".com");
        assertThrows(TimeoutException.class, () -> createBusinessTermPage.createBusinessTerm(businessTerm2));
    }

    @Test
    @DisplayName("TC_42_1_4")
    public void enduser_can_search_for_business_term_based_only_on_its_term() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPage.setTerm(randomBusinessTerm.getBusinessTerm());
        viewEditBusinessTermPage.hitSearchButton();
        assertBusinessTermNameInTheSearchResultsAtFirst(viewEditBusinessTermPage, randomBusinessTerm.getBusinessTerm(), "businessTerm");
    }

    @Test
    @DisplayName("TC_42_1_5")
    public void enduser_can_search_for_business_term_based_on_external_reference_uri() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPage.setExternalReferenceURI(randomBusinessTerm.getExternalReferenceUri());
        viewEditBusinessTermPage.hitSearchButton();
        assertBusinessTermNameInTheSearchResultsAtFirst(viewEditBusinessTermPage, randomBusinessTerm.getExternalReferenceUri(), "externalReferenceUri");
    }

    private void assertBusinessTermNameInTheSearchResultsAtFirst(ViewEditBusinessTermPage viewEditBusinessTermPage, String searchString, String columnName) {
        retry(() -> {
            WebElement tr = viewEditBusinessTermPage.getTableRecordAtIndex(1);
            WebElement td = viewEditBusinessTermPage.getColumnByName(tr, columnName);
            assertEquals(searchString, td.findElement(By.cssSelector("a > span")).getText());
        });
    }

    @Test
    @DisplayName("TC_42_1_6")
    public void enduser_can_click_business_term_to_update_its_details_in_edit_business_term_page() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        String oldTermName = randomBusinessTerm.getBusinessTerm();
        randomBusinessTerm.setBusinessTerm("bt_" + randomAlphanumeric(5, 10));
        assertNotEquals(oldTermName, randomBusinessTerm.getBusinessTerm());

        String oldExternalRefUri = randomBusinessTerm.getExternalReferenceUri();
        randomBusinessTerm.setExternalReferenceUri("http://www." + randomAscii(3, 8) + ".com");
        assertNotEquals(oldExternalRefUri, randomBusinessTerm.getExternalReferenceUri());

        String oldExternalRefID = randomBusinessTerm.getExternalReferenceId();
        randomBusinessTerm.setExternalReferenceId(randomNumeric(1, 10));
        assertNotEquals(oldExternalRefID, randomBusinessTerm.getExternalReferenceId());

        String oldComment = randomBusinessTerm.getComment();
        randomBusinessTerm.setComment(randomPrint(20, 50).trim());
        assertNotEquals(oldComment, randomBusinessTerm.getComment());

        editBusinessTermPage.updateBusinessTerm(randomBusinessTerm);

        assertThrows(NoSuchElementException.class, () -> bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(oldTermName));

        editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        assertEquals(randomBusinessTerm.getBusinessTerm(), editBusinessTermPage.getBusinessTermFieldText());
        assertEquals(randomBusinessTerm.getExternalReferenceUri(), editBusinessTermPage.getExternalReferenceURIFieldText());
        assertEquals(randomBusinessTerm.getExternalReferenceId(), editBusinessTermPage.getExternalReferenceIDFieldText());
        assertEquals(randomBusinessTerm.getComment(), editBusinessTermPage.getCommentFieldText());
    }

    @Test
    @DisplayName("TC_42_1_7")
    public void enduser_cannot_change_definition_field_in_edit_business_term_page() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu()
                .openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        WebElement definitionField = editBusinessTermPage.getDefinitionField();
        assertDisabled(definitionField);
    }

    @Test
    @DisplayName("TC_42_1_8")
    public void enduser_cannot_save_business_term_if_an_already_existing_term_and_uri_in_edit_business_term_page() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        CreateBusinessTermPage createBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openCreateBusinessTermPage();
        createBusinessTermPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        createBusinessTermPage.setExternalReferenceURI(randomBusinessTerm.getExternalReferenceUri());
        click(createBusinessTermPage.getCreateButton());
        assertTrue(getDriver().findElement(By.xpath(
                "//*[contains(text(), \"Another business term with the same business term and " +
                        "external reference URI already exists!\")]")).isDisplayed());
    }

    @Test
    @DisplayName("TC_42_1_9")
    public void enduser_cannot_discard_business_term_in_edit_business_term_page_if_it_is_used_in_assignments() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        //use pre-existing BBIE node
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");

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
        assertTrue(businessTermAssignmentPage.getTurnOffButton().isEnabled()); // check Selected BIE is enabled
        AssignBusinessTermBIEPage assignBusinessTermBIEPage = businessTermAssignmentPage.assignBusinessTerm();
        assignBusinessTermBIEPage.setTopLevelBIE(topLevelASBIEP.getPropertyTerm());
        assignBusinessTermBIEPage.hitSearchButton();
        click(assignBusinessTermBIEPage.getSelectCheckboxAtIndex(1));

        AssignBusinessTermBTPage assignBusinessTermBTPage = assignBusinessTermBIEPage.hitNextButton();
        assignBusinessTermBTPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        assignBusinessTermBTPage.hitSearchButton();
        click(assignBusinessTermBTPage.getSelectCheckboxAtIndex(1));
        click(assignBusinessTermBTPage.getCreateButton());

        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        click(editBusinessTermPage.getDiscardButton());
        WebElement confirmDiscardButton = elementToBeClickable(getDriver(), By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]"));
        click(confirmDiscardButton);
        assertEquals("Discard's forbidden! The business term is used.", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_42_1_10")
    public void enduser_can_discard_business_term_in_edit_business_term_page_if_not_used_in_any_assignments() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        //use pre-existing BBIE node
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
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
        assertTrue(businessTermAssignmentPage.getTurnOffButton().isEnabled()); // check Selected BIE is enabled
        AssignBusinessTermBIEPage assignBusinessTermBIEPage = businessTermAssignmentPage.assignBusinessTerm();
        assignBusinessTermBIEPage.setTopLevelBIE(topLevelASBIEP.getPropertyTerm());
        assignBusinessTermBIEPage.hitSearchButton();
        click(assignBusinessTermBIEPage.getSelectCheckboxAtIndex(1));

        AssignBusinessTermBTPage assignBusinessTermBTPage = assignBusinessTermBIEPage.hitNextButton();
        assignBusinessTermBTPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        assignBusinessTermBTPage.hitSearchButton();
        click(assignBusinessTermBTPage.getSelectCheckboxAtIndex(1));
        click(assignBusinessTermBTPage.getCreateButton());

        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        click(editBusinessTermPage.getDiscardButton());
        WebElement confirmDiscardButton = elementToBeClickable(getDriver(), By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]"));
        click(confirmDiscardButton);
        assertEquals("Discard's forbidden! The business term is used.", getSnackBarMessage(getDriver()));

        EditBIEPage editBIEPageForDiscard = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        WebElement bbieNodeForDiscard = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForDiscard = editBIEPage.getBBIEPanel(bbieNodeForDiscard);
        BusinessTermAssignmentPage businessTermAssignmentPageForDiscard = bbiePanelForDiscard.clickShowBusinessTermsButton();
        businessTermAssignmentPageForDiscard.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        businessTermAssignmentPageForDiscard.hitSearchButton();
        click(businessTermAssignmentPageForDiscard.getSelectCheckboxAtIndex(1));
        click(businessTermAssignmentPageForDiscard.getDiscardButton(true));
        WebElement confirmDiscardAssignmentButton = elementToBeClickable(getDriver(), By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]"));
        click(confirmDiscardAssignmentButton);

        ViewEditBusinessTermPage viewEditBusinessTermPageForDiscard = bieMenu.openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPageForDiscard.setTerm(randomBusinessTerm.getBusinessTerm());
        viewEditBusinessTermPageForDiscard.hitSearchButton();
        click(viewEditBusinessTermPageForDiscard.getSelectCheckboxAtIndex(1));
        click(viewEditBusinessTermPageForDiscard.getDiscardButton());
        WebElement confirmDiscardTermButton = elementToBeClickable(getDriver(), By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]"));
        click(confirmDiscardTermButton);
        assertThrows(NoSuchElementException.class, () -> {
            viewEditBusinessTermPageForDiscard.openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());
        });
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