package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.asccp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ASCCPCreateDialog;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertNotChecked;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_11_CreatingBrandNewDeveloperASCCP extends BaseTest {
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
    @DisplayName("TC_10_11_TA_1")
    public void brand_new_asccp_uses_expected_default_values_for_selected_acc() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals(branch, getText(asccpPanel.getReleaseField()));
        assertEquals("1", getText(asccpPanel.getRevisionField()));
        assertEquals("WIP", getText(asccpPanel.getStateField()));

        String propertyTermText = getText(asccpPanel.getPropertyTermField());
        assertEquals(asccp.getPropertyTerm(), propertyTermText);

        assertNotChecked(asccpPanel.getNillableCheckbox());
        assertDisabled(asccpPanel.getDeprecatedCheckbox());

        String namespaceText = getText(asccpPanel.getNamespaceSelectField());
        assertEquals("Namespace", namespaceText);

        String definitionText = getText(asccpPanel.getDefinitionField());
        assertTrue(isEmpty(definitionText));

        String definitionSourceText = getText(asccpPanel.getDefinitionSourceField());
        assertTrue(isEmpty(definitionSourceText));
    }

    @Test
    @DisplayName("TC_10_11_TA_2")
    public void asccp_picks_up_underlying_acc_definition_changes() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);

        acc.setDefinition("definition changed");
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        WebElement accNode = asccpViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + acc.getDen());
        ASCCPViewEditPage.ACCPanel accPanel = asccpViewEditPage.getACCPanel(accNode);
        assertEquals("definition changed", getText(accPanel.getDefinitionField()));
    }

    @Test
    @DisplayName("TC_10_11_TA_3")
    public void developer_cannot_create_brand_new_asccp_in_release_branch() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "10.8.4";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        assertThrows(TimeoutException.class, () -> viewEditCoreComponentPage.openASCCPCreateDialog(branch));

    }

    @Test
    @DisplayName("TC_10_11_TA_4")
    public void developer_cannot_create_brand_new_asccp_in_draft_release_branch() {


    }

    @Test
    @DisplayName("TC_10_11_TA_5")
    public void extension_acc_cannot_be_selected_when_creating_asccp() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        asccpCreateDialog.setDEN("Test Equipment Extension. Details");
        asccpCreateDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"Test Equipment Extension\")]//ancestor::tr/td[1]//label/span[1]")).size());
    }

    @Test
    @DisplayName("TC_10_11_TA_6")
    public void only_semantics_and_semantic_group_accs_can_be_selected_when_creating_asccp() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        asccpCreateDialog.setDEN("Issued Item Instance Base. Details");
        asccpCreateDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"Issued Item Instance Base. Details\")]")).size());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofSeconds(1L));
        asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        asccpCreateDialog.setDEN("Issued Item Instance Extension. Details");
        asccpCreateDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"Issued Item Instance Extension. Details\")]")).size());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofSeconds(1L));
        asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        asccpCreateDialog.setDEN("Any Structured Content. Details");
        asccpCreateDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"Any Structured Content. Details\")]")).size());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofSeconds(1L));
        asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        asccpCreateDialog.setDEN("OAGIS10 Nouns. Details");
        asccpCreateDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"OAGIS10 Nouns. Details\")]")).size());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofSeconds(1L));
        asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        asccpCreateDialog.setDEN("OAGIS10 BODs. Details");
        asccpCreateDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"OAGIS10 BODs. Details\")]")).size());
    }

    @Test
    @DisplayName("TC_10_11_TA_7")
    public void developer_can_create_asccp_from_acc_detail_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
        accViewEditPage.createASCCPfromThis("/" + acc.getDen());
        WebElement confirmCreateButton = elementToBeClickable(getDriver(), By.xpath("//mat-dialog-container//span[contains(text(), \"Create\")]//ancestor::button[1]"));
        click(confirmCreateButton);
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        assertTrue(acc.getDen().startsWith(asccp.getPropertyTerm()));
    }
}
