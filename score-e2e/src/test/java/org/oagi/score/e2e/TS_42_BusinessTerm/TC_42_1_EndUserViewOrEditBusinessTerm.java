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
import org.oagi.score.e2e.menu.ContextMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.business_term.*;
import org.oagi.score.e2e.page.context.EditContextSchemePage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.*;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_42_1_EndUserViewOrEditBusinessTerm extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    private class UserTopLevelASBIEPContainer {
        static List<String> SAMPLED_ASCCP_DEN_LIST = Arrays.asList(
                "Coordinate Reference. Sequenced Identifiers",
                "Account Identifiers. Named Identifiers",
                "Customer Item Identification. Item Identification",
                "Change Product Availability. Change Product Availability",
                "Collaboration Message. Collaboration Message",
                "Production Data. Production Data");
        private AppUserObject appUser;
        private int yieldPointer = 0;
        int numberOfWIPBIEs;
        int numberOfQABIEs;
        int numberOfProductionBIEs;

        private List<TopLevelASBIEPObject> randomTopLevelASBIEs = new ArrayList<>();

        public UserTopLevelASBIEPContainer(AppUserObject appUser, ReleaseObject release) {
            this(appUser, release, nextInt(1, 3), nextInt(1, 3), nextInt(1, 3));
        }

        public UserTopLevelASBIEPContainer(AppUserObject appUser, ReleaseObject release,
                                           int numberOfWIPBIEs, int numberOfQABIEs, int numberOfProductionBIEs) {
            this.appUser = appUser;
            this.numberOfWIPBIEs = numberOfWIPBIEs;
            this.numberOfQABIEs = numberOfQABIEs;
            this.numberOfProductionBIEs = numberOfProductionBIEs;

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(this.appUser);

            for (int i = 0; i < numberOfWIPBIEs; ++i) {
                String randomASCCP = nextRandomASCCP();
                TopLevelASBIEPObject topLevelASBIEP = createTopLevelASBIEPByDEN(this.appUser, context, randomASCCP, release, "WIP");
                randomTopLevelASBIEs.add(topLevelASBIEP);
            }
            for (int i = 0; i < numberOfQABIEs; ++i) {
                String randomASCCP = nextRandomASCCP();
                TopLevelASBIEPObject topLevelASBIEP = createTopLevelASBIEPByDEN(this.appUser, context, randomASCCP, release, "QA");
                randomTopLevelASBIEs.add(topLevelASBIEP);
            }
            for (int i = 0; i < numberOfProductionBIEs; ++i) {
                String randomASCCP = nextRandomASCCP();
                TopLevelASBIEPObject topLevelASBIEP = createTopLevelASBIEPByDEN(this.appUser, context, randomASCCP, release, "Production");
                randomTopLevelASBIEs.add(topLevelASBIEP);
            }
        }

        String nextRandomASCCP() {
            if (this.yieldPointer == SAMPLED_ASCCP_DEN_LIST.size()) {
                this.yieldPointer = 0;
            }
            return SAMPLED_ASCCP_DEN_LIST.get(this.yieldPointer++);
        }
    }

    private TopLevelASBIEPObject createTopLevelASBIEPByDEN(AppUserObject creator, BusinessContextObject businessContext,
                                                           String den, ReleaseObject release, String state) {
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(den, release.getReleaseNumber());
        return getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext), asccp, creator, state);
    }

    @Test
    @DisplayName("TC_42_1_1")
    public void enduser_should_open_page_titled_business_term_under_bie_menu() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        getDriver().manage().window().maximize();
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
        getDriver().manage().window().maximize();
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
        BusinessTermObject randomBusinessTerm = BusinessTermObject.createRandomBusinessTerm(endUser);
        getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(randomBusinessTerm, endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditBusinessTermPage viewEditBusinessTermPage;

        // Test 'Updater' field
        homePage.openPage();
        BIEMenu bieMenu = homePage.getBIEMenu();
        viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPage.setTerm(randomBusinessTerm.getBusinessTerm());
        viewEditBusinessTermPage.hitSearchButton();
        assertBusinessTermNameInTheSearchResultsAtFirst(
                viewEditBusinessTermPage, randomBusinessTerm.getBusinessTerm(), "businessTerm");

    }

    @Test
    @DisplayName("TC_42_1_5")
    public void enduser_can_search_for_business_term_based_on_external_reference_uri() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = BusinessTermObject.createRandomBusinessTerm(endUser);
        getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(randomBusinessTerm, endUser);
        getDriver().manage().window().maximize();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditBusinessTermPage viewEditBusinessTermPage;

        // Test 'Updater' field
        homePage.openPage();
        BIEMenu bieMenu = homePage.getBIEMenu();
        viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPage.setExternalReferenceURI(randomBusinessTerm.getExternalReferenceUri());
        viewEditBusinessTermPage.hitSearchButton();
        assertBusinessTermNameInTheSearchResultsAtFirst(
                viewEditBusinessTermPage, randomBusinessTerm.getExternalReferenceUri(), "externalReferenceUri");
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
        BusinessTermObject randomBusinessTerm =
                getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        getDriver().manage().window().maximize();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage =
                bieMenu.openViewEditBusinessTermSubMenu()
                        .openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        String oldTermName = randomBusinessTerm.getBusinessTerm();
        randomBusinessTerm.setBusinessTerm("bt_" + randomAlphanumeric(5, 10));
        assertFalse(oldTermName.equals(randomBusinessTerm.getBusinessTerm()));

        String oldExternalRefUri = randomBusinessTerm.getExternalReferenceUri();
        randomBusinessTerm.setExternalReferenceUri("http://www." + randomAscii(3,8) + ".com");
        assertFalse(oldExternalRefUri.equals(randomBusinessTerm.getExternalReferenceUri()));

        String oldExternalRefID = randomBusinessTerm.getExternalReferenceId();
        randomBusinessTerm.setExternalReferenceId(randomNumeric(1,10));
        assertFalse(oldExternalRefID.equals(randomBusinessTerm.getExternalReferenceId()));

        String oldComment = randomBusinessTerm.getComment();
        randomBusinessTerm.setComment(randomPrint(20,50).trim());
        assertFalse(oldComment.equals(randomBusinessTerm.getComment()));

        editBusinessTermPage.updateBusinessTerm(randomBusinessTerm);

        assertThrows(NoSuchElementException.class, () ->
                bieMenu.openViewEditBusinessTermSubMenu()
                        .openEditBusinessTermPageByTerm(oldTermName));

        editBusinessTermPage =
                bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        assertEquals(randomBusinessTerm.getBusinessTerm(), editBusinessTermPage.getBusinessTermFieldText());
        assertEquals(randomBusinessTerm.getExternalReferenceUri(), editBusinessTermPage.getExternalReferenceURIFieldText());
        assertEquals(randomBusinessTerm.getExternalReferenceId(), editBusinessTermPage.getExternalReferenceIDFieldText());
        assertEquals(randomBusinessTerm.getComment(),  editBusinessTermPage.getCommentFieldText());

    }

    @Test
    @DisplayName("TC_42_1_7")
    public void enduser_cannot_change_definition_field_in_edit_business_term_page() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm =
                getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        getDriver().manage().window().maximize();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage =
                bieMenu.openViewEditBusinessTermSubMenu()
                        .openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        WebElement definitionField = editBusinessTermPage.getDefinitionField();
        assertTrue(definitionField.getAttribute("readonly").equals("true"));
    }

    @Test
    @DisplayName("TC_42_1_8")
    public void enduser_cannot_save_business_term_if_an_already_existing_term_and_uri_in_edit_business_term_page() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm =
                getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        getDriver().manage().window().maximize();
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        CreateBusinessTermPage createBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openCreateBusinessTermPage();
        createBusinessTermPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        createBusinessTermPage.setExternalReferenceURI(randomBusinessTerm.getExternalReferenceUri());
        click(createBusinessTermPage.getCreateButton());
        assertTrue(getDriver().findElement(
                        By.xpath("//*[contains(text(), \"Another business term with the same business term and external reference URI already exists!\")]"))
                .isDisplayed());
    }

    @Test
    @DisplayName("TC_42_1_9")
    public void enduser_cannot_discard_business_term_in_edit_business_term_page_if_it_is_used_in_assignments() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm =
                getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        //use pre-existing BBIE node
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();
        //Assign business term to pre-existing, used BBIE node
        BusinessTermAssignmentPage businessTermAssignmentPage = bbiePanel.clickShowBusinessTermsButton();
        assertTrue(businessTermAssignmentPage.getTurnOffButton().isEnabled()); // check Selected BIE is enabled
        AssignBusinessTermBIEPage assignBusinessTermBIEPage = businessTermAssignmentPage.assignBusinessTerm();
        assignBusinessTermBIEPage.setTopLevelBIE(topLevelASBIEP.getPropertyTerm());
        click(assignBusinessTermBIEPage.getSearchButton());
        click(assignBusinessTermBIEPage.getSelectCheckboxAtIndex(0));

        AssignBusinessTermBTPage assignBusinessTermBTPage = assignBusinessTermBIEPage.hitNextButton();
        assignBusinessTermBTPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        click(assignBusinessTermBTPage.getSearchButton());
        click(assignBusinessTermBTPage.getSelectCheckboxAtIndex(0));
        click(assignBusinessTermBTPage.getCreateButton());

        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage =
                bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        assertThrows(TimeoutException.class, () -> editBusinessTermPage.discard());

        assertEquals("Discard's forbidden! The business term is used.", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_42_1_10")
    public void enduser_can_discard_business_term_in_edit_business_term_page_if_not_used_in_any_assignments() {
        //create random business term
        //create random BBIE or ABIE
        //create random assigned business term
        // try click "discard" button
        // assert Forbidden message is displayed
        //delete the assignment
        // re-click "discard" button
        // assert the business term is permanently removed.
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