package org.oagi.score.e2e.TS_20_NamespaceManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.namespace.CreateNamespacePage;
import org.oagi.score.e2e.page.namespace.EditNamespacePage;
import org.oagi.score.e2e.page.namespace.ViewEditNamespacePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;

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

        String testURI = "http://www.testenduseranamespace1.org/user/10";
        createNamespacePage.setURI(testURI);
        createNamespacePage.hitCreateButton();
        viewEditNamespacePage.openPage();
        viewEditNamespacePage.setURI(testURI);
        viewEditNamespacePage.hitSearchButton();
        WebElement tr = viewEditNamespacePage.getTableRecordAtIndex(1);
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(),\"" + testURI + "\")]//ancestor::tr[1]//td[contains(text(),\"" + endUser.getLoginId() + "\")]")).size());

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
    public void test_TA_20_2_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditNamespacePage viewEditNamespacePage = homePage.getCoreComponentMenu().openViewEditNamespaceSubMenu();
        CreateNamespacePage createNamespacePage = viewEditNamespacePage.hitNewNamespaceButton();
        assertChecked(createNamespacePage.getStandardCheckboxField());
        assertDisabled(createNamespacePage.getStandardCheckboxField());

        String testURI = "http://www.testenduseranamespace1.org/user/10";
        createNamespacePage.setURI(testURI);
        createNamespacePage.setPrefix("a prefix");
        createNamespacePage.setDescription("a description");
        createNamespacePage.hitCreateButton();
        viewEditNamespacePage.openPage();
        viewEditNamespacePage.setURI(testURI);
        viewEditNamespacePage.hitSearchButton();
        WebElement tr = viewEditNamespacePage.getTableRecordAtIndex(1);

        EditNamespacePage editNamespacePage = viewEditNamespacePage.openNamespaceByURIAndOwner(testURI, endUser.getLoginId());
        assertEquals("a prefix", getText(editNamespacePage.getPrefixField()));
        assertEquals("a description", getText(editNamespacePage.getDescriptionField()));
        assertNotChecked(editNamespacePage.getStandardCheckboxField());
        assertDisabled(editNamespacePage.getStandardCheckboxField());
        editNamespacePage.getURIField().clear();
        editNamespacePage.setURI("http://www.openapplications.org/oagis/10");
        editNamespacePage.hitUpdateButton();
        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Namespace '" + testURI + "' exists."));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));

        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(),\"" + testURI + "\")]//ancestor::tr[1]//td[contains(text(),\"" + endUser.getLoginId() + "\")]")).size());
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
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

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

    }


    @Test
    public void test_TA_20_2_5() {

    }

    @Test
    public void test_TA_20_2_6() {

    }

}
