package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.acc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_3_EditingBrandNewDeveloperACC extends BaseTest {

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
    public void test_TA_10_3_1_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertFalse(getText(accPanel.getComponentTypeSelectField()).contains("Base"));
        accPanel.setComponentType("Base (Abstract)");
        assertDisabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());
    }

    @Test
    public void test_TA_10_3_1_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Semantics"));
        assertNotChecked(accPanel.getAbstractCheckbox());
        assertEnabled(accPanel.getAbstractCheckbox());

        accPanel.setComponentType("Semantic Group");
        assertDisabled(accPanel.getAbstractCheckbox());
        assertNotChecked(accPanel.getAbstractCheckbox());
    }

    @Test
    public void test_TA_10_3_1_c() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("true", accPanel.getObjectClassTermField().getAttribute("aria-required"));
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Semantics"));
        assertEquals("true", accPanel.getNamespaceSelectField().getAttribute("aria-required"));
        assertNotChecked(accPanel.getAbstractCheckbox());

        //only standard namespace shall be allowed
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        accPanel = accViewEditPage.getACCPanel(accNode);
        ACCViewEditPage.ACCPanel finalAccPanel = accPanel;
        assertThrows(WebDriverException.class, () -> finalAccPanel.setNamespace(endUserNamespace.getUri()));

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        accPanel = accViewEditPage.getACCPanel(accNode);
        click(accPanel.getComponentTypeSelectField());
        waitFor(ofMillis(1000L));
        isHidden("//mat-option//span[.=\" Extension \"]//ancestor::mat-option");
        isHidden("//mat-option//span[.=\" User Extension Group \"]//ancestor::mat-option");
        isHidden("//mat-option//span[.=\" Embedded \"]//ancestor::mat-option");
        isHidden("//mat-option//span[.=\" OAGIS10 Nouns \"]//ancestor::mat-option");
        isHidden("//mat-option//span[.=\" OAGIS10 BODs \"]//ancestor::mat-option");
        escape(getDriver());
    }

    private void isHidden(String xpath) {
        try {
            getDriver().findElement(By.xpath(xpath + "[@hidden]"));
        } catch (Exception notPresent) {
            waitFor(ofMillis(2000L));
            assertTrue(!isElementPresent(getDriver(), By.xpath(xpath)));
        }
    }

    @Test
    public void test_TA_10_3_1_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("1", getText(accPanel.getRevisionField()));
        assertNotChecked(accPanel.getDeprecatedCheckbox());
        assertDisabled(accPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_3_1_e() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        acc.setDefinition(null);
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        accPanel.setObjectClassTerm(randomPropertyTerm);
        String namespaceForUpdate = "http://www.openapplications.org/oagis/10";
        accPanel.setNamespace(namespaceForUpdate);

        assertThrows(TimeoutException.class, () -> accViewEditPage.hitUpdateButton());
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]"))));

    }

    @Test
    public void test_TA_10_3_1_f() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPObject randomASCCP1, randomASCCP2;
        randomASCCP1 = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc, developer, namespace, "WIP");
        randomASCCP2 = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc, developer, namespace, "WIP");

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        randomPropertyTerm = "Test Object " + randomPropertyTerm;

        acc.setObjectClassTerm(randomPropertyTerm);
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        {
            viewEditCoreComponentPage.openPage();
            waitFor(ofSeconds(1L));
            ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(randomASCCP1.getAsccpManifestId());
            ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
            String asccpDEN = getText(asccpPanel.getDENField());
            assertTrue(asccpDEN.endsWith(randomPropertyTerm));
            assertEquals("1", getText(asccpPanel.getRevisionField()));
        }

        {
            viewEditCoreComponentPage.openPage();
            waitFor(ofSeconds(1L));
            ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(randomASCCP2.getAsccpManifestId());
            ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
            String asccpDEN = getText(asccpPanel.getDENField());
            assertTrue(asccpDEN.endsWith(randomPropertyTerm));
            assertEquals("1", getText(asccpPanel.getRevisionField()));
        }

    }

    @Test
    public void test_TA_10_3_1_g() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        accPanel = accViewEditPage.getACCPanel(accNode);
        click(accPanel.getComponentTypeSelectField());
        waitFor(ofMillis(1000L));
        assertEquals(1, getDriver().findElements(By.xpath("//mat-option//span[.=\" Base (Abstract) \"]//ancestor::mat-option")).size());
        assertEquals(1, getDriver().findElements(By.xpath("//mat-option//span[.=\" Semantic Group \"]//ancestor::mat-option")).size());
        assertEquals(1, getDriver().findElements(By.xpath("//mat-option//span[.=\" Semantics \"]//ancestor::mat-option")).size());
        escape(getDriver());
    }

    @Test
    public void test_TA_10_3_2_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        // cannot create extension component when the component type isn't Semantics
        String accOwner = getText(accPanel.getOwnerField());
        accPanel.setComponentType("Base (Abstract)");
        accPanel.setNamespace(namespace.getUri());
        accViewEditPage.hitUpdateButton();
        accViewEditPage.clickOnDropDownMenuByPath("/" + acc.getDen());

        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(), \"Create OAGi Extension Component\")]")).size());
        escape(getDriver());
        accPanel = accViewEditPage.getACCPanel(accNode);
        accPanel.setComponentType("Semantics");
        accViewEditPage.hitUpdateButton();
        accViewEditPage.createOAGiExtensionComponent("/" + acc.getDen());
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Create\")]//ancestor::button[1]")));

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement extensionNode = accViewEditPage.getNodeByPath("/" + acc.getObjectClassTerm() + " Extension. Details");
        ACCViewEditPage.ACCPanel extensionACCPanel = accViewEditPage.getACCPanel(extensionNode);
        String extensionObjectClassTerm = getText(extensionACCPanel.getObjectClassTermField());
        assertEquals(acc.getObjectClassTerm() + " Extension", extensionObjectClassTerm);
        assertEquals("Extension", getText(extensionACCPanel.getComponentTypeSelectField()));
        assertEquals(accOwner, getText(extensionACCPanel.getOwnerField()));

        String extensionDen = getText(extensionACCPanel.getDENField());
        WebElement allExtensionBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Extension/" + extensionDen + "/All Extension. Details");
        ACCViewEditPage.ACCPanel allExtensionBasePanel = accViewEditPage.getACCPanel(allExtensionBaseNode);
        assertEquals("All Extension. Details", getText(allExtensionBasePanel.getDENField()));
    }

    @Test
    public void test_TA_10_3_2_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        // cannot create extension component when the component type isn't Semantics
        String accOwner = getText(accPanel.getOwnerField());
        accPanel.setComponentType("Base (Abstract)");
        accPanel.setNamespace(namespace.getUri());
        accViewEditPage.hitUpdateButton();
        accViewEditPage.clickOnDropDownMenuByPath("/" + acc.getDen());

        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(), \"Create OAGi Extension Component\")]")).size());
        escape(getDriver());
        accPanel = accViewEditPage.getACCPanel(accNode);
        accPanel.setComponentType("Semantics");
        accViewEditPage.hitUpdateButton();
        accViewEditPage.createOAGiExtensionComponent("/" + acc.getDen());
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Create\")]//ancestor::button[1]")));

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement extensionASCCNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Extension");
        ACCViewEditPage.ASCCPPanel extensionASCCPPanel = accViewEditPage.getASCCPanelContainer(extensionASCCNode).getASCCPPanel();
        assertEquals("ASCCP", getText(extensionASCCPPanel.getCoreComponentField()));
        assertEquals("Extension", getText(extensionASCCPPanel.getPropertyTermField()));
        assertTrue(getText(extensionASCCPPanel.getDENField()).endsWith(acc.getObjectClassTerm() + " Extension"));
    }

    @Test
    public void test_TA_10_3_2_c() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        // cannot create extension component when the component type isn't Semantics
        String accOwner = getText(accPanel.getOwnerField());
        accPanel.setComponentType("Base (Abstract)");
        accPanel.setNamespace(namespace.getUri());
        accViewEditPage.hitUpdateButton();
        accViewEditPage.clickOnDropDownMenuByPath("/" + acc.getDen());

        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(), \"Create OAGi Extension Component\")]")).size());
        escape(getDriver());
        accPanel = accViewEditPage.getACCPanel(accNode);
        accPanel.setComponentType("Semantics");
        accViewEditPage.hitUpdateButton();
        accViewEditPage.createOAGiExtensionComponent("/" + acc.getDen());
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Create\")]//ancestor::button[1]")));

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement extensionASCCNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Extension");
        ACCViewEditPage.ASCCPanel extensionASCCPanel = accViewEditPage.getASCCPanelContainer(extensionASCCNode).getASCCPanel();
        assertEquals("ASCC", getText(extensionASCCPanel.getCoreComponentField()));
        assertEquals("0", getText(extensionASCCPanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(extensionASCCPanel.getCardinalityMaxField()));
    }

    @Test
    public void test_TA_10_3_2_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.createACC(branch);
        String url = getDriver().getCurrentUrl();
        BigInteger accManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);

        // cannot create extension component when the namespace is not set
        accViewEditPage.clickOnDropDownMenuByPath("/" + acc.getDen());
        assertEquals(1, getDriver().findElements(By.xpath("//span[contains(text(), \"Create OAGi Extension Component\")]")).size());
        escape(getDriver());
        accViewEditPage.createOAGiExtensionComponent("/" + acc.getDen());
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Create\")]//ancestor::button[1]")));
        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Namespace is required."));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));

    }
}
