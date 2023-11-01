package org.oagi.score.e2e.TS_43_OpenAPIDocument;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.oas.CreateOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_43_1_DefineOpenAPIDocumentDefinition extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_43_1_1")
    public void enduser_should_open_page_titled_OpenAPI_document_under_bie_menu() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        String openAPIDocumentPageTitle = getText(bieMenu.openOpenAPIDocumentSubMenu().getTitle());
        assertEquals("OpenAPI Document", openAPIDocumentPageTitle);
    }

    @Test
    @DisplayName("TC_43_1_2")
    public void enduser_can_create_OpenAPI_document_with_only_required_fields() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        CreateOpenAPIDocumentPage createOpenAPIDocumentPage = openAPIDocumentPage.openCreateOpenAPIDocumentPage();

        String openApiVersion = "3.0.3";
        String randomTitle = "oas_doc_" + randomAlphanumeric(5, 10);
        String randomDocumentVersion = "oas_doc_ver_" + randomNumeric(3, 7);

        createOpenAPIDocumentPage.setOpenAPIVersion(openApiVersion);
        createOpenAPIDocumentPage.setTitle(randomTitle);
        createOpenAPIDocumentPage.setDocumentVersion(randomDocumentVersion);
        openAPIDocumentPage = createOpenAPIDocumentPage.create();

        openAPIDocumentPage.setTitle(randomTitle);
        openAPIDocumentPage.hitSearchButton();

        WebElement tr = openAPIDocumentPage.getTableRecordAtIndex(1);
        assertNotNull(tr);

        WebElement tdVersion = openAPIDocumentPage.getColumnByName(tr, "version");
        assertEquals(randomDocumentVersion, getText(tdVersion));
    }

    @Test
    @DisplayName("TC_43_1_3")
    public void enduser_cannot_create_OpenAPI_document_if_any_required_field_is_not_provided() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        CreateOpenAPIDocumentPage createOpenAPIDocumentPage = openAPIDocumentPage.openCreateOpenAPIDocumentPage();

        String randomTitle = "oas_doc_" + randomAlphanumeric(5, 10);
        createOpenAPIDocumentPage.setTitle(randomTitle);
        assertThrows(TimeoutException.class, () -> createOpenAPIDocumentPage.create());

        createOpenAPIDocumentPage.openPage();
        String randomDocumentVersion = "oas_doc_ver_" + randomNumeric(3, 7);
        createOpenAPIDocumentPage.setDocumentVersion(randomDocumentVersion);
        assertThrows(TimeoutException.class, () -> createOpenAPIDocumentPage.create());
    }

    @Test
    @DisplayName("TC_43_1_4")
    public void enduser_can_search_for_OpenAPI_document_based_only_on_its_title() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        List<OpenAPIDocumentObject> openAPIDocuments = Arrays.asList(
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser),
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser),
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser));

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();

        for (OpenAPIDocumentObject openAPIDocument : openAPIDocuments) {
            openAPIDocumentPage.setTitle(openAPIDocument.getTitle());
            openAPIDocumentPage.hitSearchButton();

            WebElement tr = openAPIDocumentPage.getTableRecordAtIndex(1);
            assertNotNull(tr);

            WebElement tdOpenAPIVersion = openAPIDocumentPage.getColumnByName(tr, "openAPIVersion");
            assertEquals(openAPIDocument.getOpenApiVersion(), getText(tdOpenAPIVersion));

            WebElement tdVersion = openAPIDocumentPage.getColumnByName(tr, "version");
            assertEquals(openAPIDocument.getVersion(), getText(tdVersion));

            WebElement tdLicenseName = openAPIDocumentPage.getColumnByName(tr, "licenseName");
            assertEquals(openAPIDocument.getLicenseName(), getText(tdLicenseName));

            WebElement tdDescription = openAPIDocumentPage.getColumnByName(tr, "description");
            assertEquals(openAPIDocument.getDescription(), getText(tdDescription));

            openAPIDocumentPage.openPage();
        }
    }

    @Test
    @DisplayName("TC_43_1_5")
    public void enduser_can_search_for_OpenAPI_document_based_on_the_description() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        List<OpenAPIDocumentObject> openAPIDocuments = Arrays.asList(
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser),
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser),
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser));

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();

        for (OpenAPIDocumentObject openAPIDocument : openAPIDocuments) {
            openAPIDocumentPage.setDescription(openAPIDocument.getDescription());
            openAPIDocumentPage.hitSearchButton();

            WebElement tr = openAPIDocumentPage.getTableRecordAtIndex(1);
            assertNotNull(tr);

            WebElement tdOpenAPIVersion = openAPIDocumentPage.getColumnByName(tr, "openAPIVersion");
            assertEquals(openAPIDocument.getOpenApiVersion(), getText(tdOpenAPIVersion));

            WebElement tdVersion = openAPIDocumentPage.getColumnByName(tr, "version");
            assertEquals(openAPIDocument.getVersion(), getText(tdVersion));

            WebElement tdLicenseName = openAPIDocumentPage.getColumnByName(tr, "licenseName");
            assertEquals(openAPIDocument.getLicenseName(), getText(tdLicenseName));

            WebElement tdDescription = openAPIDocumentPage.getColumnByName(tr, "description");
            assertEquals(openAPIDocument.getDescription(), getText(tdDescription));

            openAPIDocumentPage.openPage();
        }
    }

    @Test
    @DisplayName("TC_43_1_6")
    public void enduser_can_open_edit_OpenAPI_document_to_update_its_details() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        OpenAPIDocumentObject randomOpenAPIDocument = getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        EditOpenAPIDocumentPage editOpenAPIDocumentPage = openAPIDocumentPage.openEditOpenAPIDocumentPage(randomOpenAPIDocument);

        assertEquals(randomOpenAPIDocument.getTitle(), getText(editOpenAPIDocumentPage.getTitleField()));
        assertEquals(randomOpenAPIDocument.getVersion(), getText(editOpenAPIDocumentPage.getDocumentVersionField()));
        assertEquals(randomOpenAPIDocument.getTermsOfService(), getText(editOpenAPIDocumentPage.getTermsOfServiceField()));
        assertEquals(randomOpenAPIDocument.getContactName(), getText(editOpenAPIDocumentPage.getContactNameField()));
        assertEquals(randomOpenAPIDocument.getContactUrl(), getText(editOpenAPIDocumentPage.getContactURLField()));
        assertEquals(randomOpenAPIDocument.getContactEmail(), getText(editOpenAPIDocumentPage.getContactEmailField()));
        assertEquals(randomOpenAPIDocument.getLicenseName(), getText(editOpenAPIDocumentPage.getLicenseNameField()));
        assertEquals(randomOpenAPIDocument.getLicenseUrl(), getText(editOpenAPIDocumentPage.getLicenseURLField()));
        assertEquals(randomOpenAPIDocument.getDescription(), getText(editOpenAPIDocumentPage.getDescriptionField()));
    }

    @Test
    @DisplayName("TC_43_1_7")
    public void enduser_can_change_fields_in_edit_OpenAPI_document() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        OpenAPIDocumentObject randomOpenAPIDocument = getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        EditOpenAPIDocumentPage editOpenAPIDocumentPage = openAPIDocumentPage.openEditOpenAPIDocumentPage(randomOpenAPIDocument);

        BigInteger oasDocId = randomOpenAPIDocument.getOasDocId();
        randomOpenAPIDocument = OpenAPIDocumentObject.createRandomOpenAPIDocument(endUser);
        randomOpenAPIDocument.setOasDocId(oasDocId);

        editOpenAPIDocumentPage.setTitle(randomOpenAPIDocument.getTitle());
        editOpenAPIDocumentPage.setDocumentVersion(randomOpenAPIDocument.getVersion());
        editOpenAPIDocumentPage.setTermsOfService(randomOpenAPIDocument.getTermsOfService());
        editOpenAPIDocumentPage.setContactName(randomOpenAPIDocument.getContactName());
        editOpenAPIDocumentPage.setContactURL(randomOpenAPIDocument.getContactUrl());
        editOpenAPIDocumentPage.setContactEmail(randomOpenAPIDocument.getContactEmail());
        editOpenAPIDocumentPage.setLicenseName(randomOpenAPIDocument.getLicenseName());
        editOpenAPIDocumentPage.setLicenseURL(randomOpenAPIDocument.getLicenseUrl());
        editOpenAPIDocumentPage.setDescription(randomOpenAPIDocument.getDescription());
        editOpenAPIDocumentPage.hitUpdateButton();

        openAPIDocumentPage.openPage();
        editOpenAPIDocumentPage = openAPIDocumentPage.openEditOpenAPIDocumentPage(randomOpenAPIDocument);

        assertEquals(randomOpenAPIDocument.getTitle(), getText(editOpenAPIDocumentPage.getTitleField()));
        assertEquals(randomOpenAPIDocument.getVersion(), getText(editOpenAPIDocumentPage.getDocumentVersionField()));
        assertEquals(randomOpenAPIDocument.getTermsOfService(), getText(editOpenAPIDocumentPage.getTermsOfServiceField()));
        assertEquals(randomOpenAPIDocument.getContactName(), getText(editOpenAPIDocumentPage.getContactNameField()));
        assertEquals(randomOpenAPIDocument.getContactUrl(), getText(editOpenAPIDocumentPage.getContactURLField()));
        assertEquals(randomOpenAPIDocument.getContactEmail(), getText(editOpenAPIDocumentPage.getContactEmailField()));
        assertEquals(randomOpenAPIDocument.getLicenseName(), getText(editOpenAPIDocumentPage.getLicenseNameField()));
        assertEquals(randomOpenAPIDocument.getLicenseUrl(), getText(editOpenAPIDocumentPage.getLicenseURLField()));
        assertEquals(randomOpenAPIDocument.getDescription(), getText(editOpenAPIDocumentPage.getDescriptionField()));
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
