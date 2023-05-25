package org.oagi.score.e2e.TS_20_NamespaceManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.namespace.CreateNamespacePage;
import org.oagi.score.e2e.page.namespace.EditNamespacePage;
import org.oagi.score.e2e.page.namespace.TransferNamespaceOwershipDialog;
import org.oagi.score.e2e.page.namespace.ViewEditNamespacePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertChecked;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_20_1_DeveloperManagementOFNamespaces extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    public void test_TA_20_1_1_a_b_c_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();
        CreateNamespacePage createNamespacePage = viewEditNamespacePage.hitNewNamespaceButton();
        assertChecked(createNamespacePage.getStandardCheckboxField());
        assertDisabled(createNamespacePage.getStandardCheckboxField());

        String randomDomain = randomAlphabetic(5, 10);
        String testURI = "https://test." + randomDomain + ".com";
        createNamespacePage.setURI(testURI);
        createNamespacePage.hitCreateButton();
        viewEditNamespacePage.openPage();
        viewEditNamespacePage.setURI(testURI);
        viewEditNamespacePage.hitSearchButton();
        WebElement tr = viewEditNamespacePage.getTableRecordAtIndex(1);
        String namespaceXpath = "//*[contains(text(),\""+testURI+"\")]//ancestor::tr[1]//span[contains(text(),\""+developer.getLoginId()+"\")]";
        assertEquals(1, getDriver().findElements(By.xpath(namespaceXpath)).size());

        createNamespacePage.openPage();
        createNamespacePage.setURI(testURI);
        createNamespacePage.hitCreateButton();
        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Namespace '" + testURI + "' exists."));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));

    }

    @Test
    public void test_TA_20_1_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();
        CreateNamespacePage createNamespacePage = viewEditNamespacePage.hitNewNamespaceButton();
        assertChecked(createNamespacePage.getStandardCheckboxField());
        assertDisabled(createNamespacePage.getStandardCheckboxField());

        String randomDomain = randomAlphabetic(5, 10);
        String testURI = "https://test." + randomDomain + ".com";
        createNamespacePage.setURI(testURI);
        createNamespacePage.setPrefix(randomDomain);
        createNamespacePage.setDescription("a description");
        createNamespacePage.hitCreateButton();
        viewEditNamespacePage.openPage();
        viewEditNamespacePage.setURI(testURI);
        viewEditNamespacePage.hitSearchButton();
        WebElement tr = viewEditNamespacePage.getTableRecordAtIndex(1);

        EditNamespacePage editNamespacePage = viewEditNamespacePage.openNamespaceByURIAndOwner(testURI, developer.getLoginId());
        assertEquals(randomDomain, getText(editNamespacePage.getPrefixField()));
        assertEquals("a description", getText(editNamespacePage.getDescriptionField()));
        assertChecked(editNamespacePage.getStandardCheckboxField());
        assertDisabled(editNamespacePage.getStandardCheckboxField());
        editNamespacePage.getURIField().clear();
        editNamespacePage.setURI("http://www.openapplications.org/oagis/10");
        editNamespacePage.hitUpdateButton();
        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Namespace '" + testURI + "' exists."));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));

        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(),\"" + testURI + "\")]//ancestor::tr[1]//span[contains(text(),\"" + developer.getLoginId() + "\")]")).size());
    }

    @Test
    public void test_TA_20_1_3_and_5() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();

        EditNamespacePage editNamespacePage = viewEditNamespacePage.openNamespaceByURIAndOwner(euNamespace.getUri(), endUser.getLoginId());
        assertDisabled(editNamespacePage.getURIField());
        assertDisabled(editNamespacePage.getPrefixField());
        assertDisabled(editNamespacePage.getDescriptionField());
        assertChecked(editNamespacePage.getStandardCheckboxField());
        assertDisabled(editNamespacePage.getStandardCheckboxField());

        viewEditNamespacePage.openPage();
        editNamespacePage = viewEditNamespacePage.openNamespaceByURIAndOwner("http://www.openapplications.org/oagis/10", "oagis");
        assertDisabled(editNamespacePage.getURIField());
        assertDisabled(editNamespacePage.getPrefixField());
        assertDisabled(editNamespacePage.getDescriptionField());
        assertChecked(editNamespacePage.getStandardCheckboxField());
        assertDisabled(editNamespacePage.getStandardCheckboxField());
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Discard\")]//ancestor::button[1]")).size());
        homePage.logout();

        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();
        editNamespacePage = viewEditNamespacePage.openNamespaceByURIAndOwner(developerNamespace.getUri(), developer.getLoginId());
        assertDisabled(editNamespacePage.getURIField());
        assertDisabled(editNamespacePage.getPrefixField());
        assertDisabled(editNamespacePage.getDescriptionField());
        assertChecked(editNamespacePage.getStandardCheckboxField());
        assertDisabled(editNamespacePage.getStandardCheckboxField());
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Discard\")]//ancestor::button[1]")).size());
        homePage.logout();
    }

    @Test
    public void test_TA_20_1_4() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();

        EditNamespacePage editNamespacePage = viewEditNamespacePage.openNamespaceByURIAndOwner(developerNamespace.getUri(), developer.getLoginId());
        editNamespacePage.hitDiscardButton();

        viewEditNamespacePage.openPage();
        assertThrows(TimeoutException.class, () -> viewEditNamespacePage.openNamespaceByURIAndOwner(developerNamespace.getUri(), developer.getLoginId()));
    }

    @Test
    public void test_TA_20_1_6() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(anotherDeveloper);
        HomePage homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();

        {
            viewEditNamespacePage.setURI(namespace.getUri());
            viewEditNamespacePage.hitSearchButton();

            WebElement tr = viewEditNamespacePage.getTableRecordByValue(namespace.getUri());
            WebElement td = viewEditNamespacePage.getColumnByName(tr, "transferOwnership");
            assertTrue(td.findElement(By.className("mat-icon")).isEnabled());

            TransferNamespaceOwershipDialog transferNamespaceOwershipDialog =
                    viewEditNamespacePage.openTransferNamespaceOwnershipDialog(tr);
            transferNamespaceOwershipDialog.transfer(developer.getLoginId());

            viewEditNamespacePage.setURI(namespace.getUri());
            viewEditNamespacePage.hitSearchButton();

            tr = viewEditNamespacePage.getTableRecordByValue(namespace.getUri());
            td = viewEditNamespacePage.getColumnByName(tr, "owner");
            assertEquals(developer.getLoginId(), getText(td));
        }
    }
}
