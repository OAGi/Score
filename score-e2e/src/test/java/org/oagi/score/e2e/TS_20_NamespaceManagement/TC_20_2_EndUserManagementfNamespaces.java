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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_20_2_EndUserManagementfNamespaces extends BaseTest {
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
    public void test_TA_20_2_1_a_b_c_d() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();
        CreateNamespacePage createNamespacePage = viewEditNamespacePage.hitNewNamespaceButton();
        assertNotChecked(createNamespacePage.getStandardCheckboxField());
        assertDisabled(createNamespacePage.getStandardCheckboxField());

        String randomDomain = randomAlphabetic(5, 10);
        String testURI = "https://test." + randomDomain + ".com";
        createNamespacePage.setURI(testURI);
        createNamespacePage.hitCreateButton();
        viewEditNamespacePage.openPage();
        viewEditNamespacePage.setURI(testURI);
        viewEditNamespacePage.hitSearchButton();
        WebElement tr = viewEditNamespacePage.getTableRecordAtIndex(1);
        String namespaceXpath = "//*[contains(text(),\"" + testURI + "\")]//ancestor::tr[1]//span[contains(text(),\"" + endUser.getLoginId() + "\")]";
        assertEquals(1, getDriver().findElements(By.xpath(namespaceXpath)).size());

        createNamespacePage.openPage();
        createNamespacePage.setURI(testURI);
        createNamespacePage.hitCreateButton();
        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Namespace '" + testURI + "' exists."));
        click(elementToBeClickable(getDriver(), By.xpath("//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));
    }

    @Test
    public void test_TA_20_2_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();
        CreateNamespacePage createNamespacePage = viewEditNamespacePage.hitNewNamespaceButton();
        assertNotChecked(createNamespacePage.getStandardCheckboxField());
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

        EditNamespacePage editNamespacePage = viewEditNamespacePage.openNamespaceByURIAndOwner(testURI, endUser.getLoginId());
        assertEquals(randomDomain, getText(editNamespacePage.getPrefixField()));
        assertEquals("a description", getText(editNamespacePage.getDescriptionField()));
        assertNotChecked(editNamespacePage.getStandardCheckboxField());
        assertDisabled(editNamespacePage.getStandardCheckboxField());
        editNamespacePage.getURIField().clear();
        String existingURI = "http://www.openapplications.org/oagis/10";
        editNamespacePage.setURI(existingURI);
        editNamespacePage.hitUpdateButton();
        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Namespace URI '" + existingURI + "' exists."));
        click(elementToBeClickable(getDriver(), By.xpath("//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));
        editNamespacePage.hitBackButton();
        waitFor(Duration.ofMillis(3000L));
        String namespaceXpath = "//*[contains(text(),\"" + testURI + "\")]//ancestor::tr[1]//span[contains(text(),\"" + endUser.getLoginId() + "\")]";
        assertEquals(1, getDriver().findElements(By.xpath(namespaceXpath)).size());
    }


    @Test
    public void test_TA_20_2_3() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();

        EditNamespacePage editNamespacePage = viewEditNamespacePage.openNamespaceByURIAndOwner(euNamespace.getUri(), endUser.getLoginId());
        assertDisabled(editNamespacePage.getURIField());
        assertDisabled(editNamespacePage.getPrefixField());
        assertDisabled(editNamespacePage.getDescriptionField());
        assertDisabled(editNamespacePage.getStandardCheckboxField());
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Discard\")]//ancestor::button[1]")).size());

        homePage.logout();
        homePage = loginPage().signIn(anotherUser.getLoginId(), anotherUser.getPassword());

        viewEditNamespacePage.openPage();
        editNamespacePage = viewEditNamespacePage.openNamespaceByURIAndOwner(euNamespace.getUri(), endUser.getLoginId());
        assertDisabled(editNamespacePage.getURIField());
        assertDisabled(editNamespacePage.getPrefixField());
        assertDisabled(editNamespacePage.getDescriptionField());
        assertDisabled(editNamespacePage.getStandardCheckboxField());
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Discard\")]//ancestor::button[1]")).size());
        homePage.logout();
    }

    @Test
    public void test_TA_20_2_4() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();

        EditNamespacePage editNamespacePage = viewEditNamespacePage.openNamespaceByURIAndOwner(euNamespace.getUri(), endUser.getLoginId());
        editNamespacePage.hitDiscardButton();

        viewEditNamespacePage.openPage();
        assertThrows(NoSuchElementException.class, () -> viewEditNamespacePage.openNamespaceByURIAndOwner(euNamespace.getUri(), endUser.getLoginId()));
    }

    @Test
    public void test_TA_20_2_6() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        HomePage homePage = loginPage().signIn(anotherUser.getLoginId(), anotherUser.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();
        CreateNamespacePage createNamespacePage = viewEditNamespacePage.hitNewNamespaceButton();
        String randomDomain = randomAlphabetic(5, 10);
        String testURI = "https://test." + randomDomain + ".com";
        createNamespacePage.setURI(testURI);
        createNamespacePage.hitCreateButton();

        {
            viewEditNamespacePage.setURI(testURI);
            viewEditNamespacePage.hitSearchButton();

            WebElement tr = viewEditNamespacePage.getTableRecordByValue(testURI);
            WebElement td = viewEditNamespacePage.getColumnByName(tr, "transferOwnership");
            assertTrue(td.findElement(By.className("mat-icon")).isEnabled());

            TransferNamespaceOwershipDialog transferNamespaceOwershipDialog = viewEditNamespacePage.openTransferNamespaceOwnershipDialog(tr);
            transferNamespaceOwershipDialog.transfer(endUser.getLoginId());

            viewEditNamespacePage.setURI(testURI);
            viewEditNamespacePage.hitSearchButton();

            tr = viewEditNamespacePage.getTableRecordByValue(testURI);
            td = viewEditNamespacePage.getColumnByName(tr, "owner");
            assertEquals(endUser.getLoginId(), getText(td));
        }

    }

}
