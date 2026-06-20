package org.oagi.score.e2e.TS_24_EndUserBIEManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.*;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_24_1_ReuseBIE extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

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

    private void assertReusedBIE(EditBIEPage editBIEPage, String path) {
        editBIEPage.clickOnDropDownMenuByPath(path);
        assertEquals(1, getDriver().findElements(By.xpath("//span[contains(text(), \"Remove Reused BIE\")]")).size());
        escape(getDriver());
    }

    private void assertBlockedStateTransition(String targetState) {
        WebElement updateButton = visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]"));
        assertTrue(!updateButton.isEnabled());

        String validationSummary = getText(visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-dialog-container//*[contains(@class, \"validation-summary\")]")));
        assertTrue(validationSummary.contains("cannot move to '" + targetState + "'"));

        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Cancel\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    private void installBrowserDownloadCapture() {
        ((JavascriptExecutor) getDriver()).executeScript(
                "window.__scoreLastDownload = null;" +
                        "if (!window.__scoreDownloadHookInstalled) {" +
                        "  const originalClick = HTMLAnchorElement.prototype.click;" +
                        "  HTMLAnchorElement.prototype.click = function() {" +
                        "    if (this.download) {" +
                        "      window.__scoreLastDownload = this.download;" +
                        "    }" +
                        "    return originalClick.apply(this, arguments);" +
                        "  };" +
                        "  window.__scoreDownloadHookInstalled = true;" +
                        "}");
    }

    private String waitForCapturedDownloadFilename() {
        return org.oagi.score.e2e.impl.PageHelper.wait(getDriver(), Duration.ofSeconds(30L), ofMillis(100L)).until(driver -> {
            Object result = ((JavascriptExecutor) driver).executeScript("return window.__scoreLastDownload;");
            if (result instanceof String && !((String) result).isBlank()) {
                return (String) result;
            }
            return null;
        });
    }

    @Test
    public void reuse_a_bie_the_reuse_target_node_can_only_be_asbie_asbiep_abie_node() {
        ASCCPObject asccp, asccp_owner_usera, asccp_to_append, asccp_child, asccp_reuse;
        BCCPObject bccp, bccp_to_append, bccp_child, bccp_not_reuse;
        ACCObject acc;
        AppUserObject usera;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE;
        String prev_release = "10.8.6";
        ReleaseObject prevReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, prev_release);
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, prevReleaseObject, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", prevReleaseObject.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(prevReleaseObject, dataType, usera, namespace, "Production");
            bccp_child = coreComponentAPI.createRandomBCCP(prevReleaseObject, dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            DTObject dataTypeWithSC = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "3292eaa5630b48ecb7c4249b0ddc760e", prevReleaseObject.getReleaseNumber());
            bccp_not_reuse = coreComponentAPI.createRandomBCCP(prevReleaseObject, dataTypeWithSC, usera, namespace, "Production");

            ACCObject acc_association = coreComponentAPI.createRandomACC(usera, prevReleaseObject, namespace, "Production");
            ACCObject acc_association2 = coreComponentAPI.createRandomACC(usera, prevReleaseObject, namespace, "Production");

            bccp_to_append = coreComponentAPI.createRandomBCCP(prevReleaseObject, dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp_child = coreComponentAPI.createRandomASCCP(acc_association, usera, namespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association2, asccp_child, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);

            asccp = coreComponentAPI.createRandomASCCP(acc_association, usera, namespace, "Production");
            asccp_to_append = coreComponentAPI.createRandomASCCP(acc_association, usera, namespace, "Production");
            asccp_reuse = coreComponentAPI.createRandomASCCP(acc_association2, usera, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            coreComponentAPI.appendExtension(acc, usera, namespace, "Published");
            asccp_owner_usera = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp_reuse, usera, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(asccp_owner_usera.getDen(), prev_release);
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_owner_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp_owner_usera.getPropertyTerm() + "/Extension");
        SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp_owner_usera.getPropertyTerm() + " User Extension Group. Details");
        selectCCPropertyPage.selectAssociation(asccp_to_append.getDen());
        selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp_owner_usera.getPropertyTerm() + " User Extension Group. Details");
        selectCCPropertyPage.selectAssociation(bccp_not_reuse.getDen());
        selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp_owner_usera.getPropertyTerm() + " User Extension Group. Details");
        selectCCPropertyPage.selectAssociation(asccp_reuse.getDen());
        ACCExtensionViewEditPage.setNamespace(namespace);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        ACCExtensionViewEditPage.moveToProduction();
        editBIEPage.openPage();
        editBIEPage.clickOnDropDownMenuByPath("/" + asccp_owner_usera.getPropertyTerm() + "/Extension/" + bccp_not_reuse.getPropertyTerm());
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Reuse BIE\")]")).size());

        escape(getDriver());
        editBIEPage.openPage();
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_owner_usera.getPropertyTerm() + "/Extension/" + asccp_reuse.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        assertReusedBIE(editBIEPage, "/" + asccp_owner_usera.getPropertyTerm() + "/Extension/" + asccp_reuse.getPropertyTerm());
    }

    @Test
    public void reuse_a_bie_top_level_bies_in_any_state_owned_by_any_user_i() {
        ASCCPObject asccp, asccp_owner_usera, asccp_to_append, asccp_child, asccp_reuse;
        BCCPObject bccp, bccp_to_append, bccp_child, bccp_not_reuse;
        ACCObject acc;
        AppUserObject usera;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", current_release);
            bccp = coreComponentAPI.createRandomBCCP(currentReleaseObject, dataType, usera, namespace, "Production");
            bccp_child = coreComponentAPI.createRandomBCCP(currentReleaseObject, dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            DTObject dataTypeWithSC = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "3292eaa5630b48ecb7c4249b0ddc760e", current_release);
            bccp_not_reuse = coreComponentAPI.createRandomBCCP(currentReleaseObject, dataTypeWithSC, usera, namespace, "Production");

            ACCObject acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");
            ACCObject acc_association2 = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");

            bccp_to_append = coreComponentAPI.createRandomBCCP(currentReleaseObject, dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp_child = coreComponentAPI.createRandomASCCP(acc_association, usera, namespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association2, asccp_child, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);

            asccp = coreComponentAPI.createRandomASCCP(acc_association, usera, namespace, "Production");
            asccp_to_append = coreComponentAPI.createRandomASCCP(acc_association, usera, namespace, "Production");
            asccp_reuse = coreComponentAPI.createRandomASCCP(acc_association2, usera, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp_reuse, "Production");
            coreComponentAPI.appendExtension(acc, usera, namespace, "Published");
            asccp_owner_usera = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp_reuse, usera, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(asccp_owner_usera.getDen(), current_release);
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_owner_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_owner_usera.getPropertyTerm() + "/" + asccp_reuse.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_owner_usera.getPropertyTerm() + "/" + asccp_reuse.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp_reuse.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());
    }

    @Test
    public void on_the_detail_pane_of_a_reused_bie_node_business_term_context_definition_remark() {
        ASCCPObject asccp_owner_usera, asccp_to_append, asccp_child, asccp_reuse;
        BCCPObject bccp, bccp_to_append, bccp_child, bccp_not_reuse;
        ACCObject acc;
        AppUserObject usera;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", current_release);
            bccp = coreComponentAPI.createRandomBCCP(currentReleaseObject, dataType, usera, namespace, "Production");
            bccp_child = coreComponentAPI.createRandomBCCP(currentReleaseObject, dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            DTObject dataTypeWithSC = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "3292eaa5630b48ecb7c4249b0ddc760e", current_release);
            bccp_not_reuse = coreComponentAPI.createRandomBCCP(currentReleaseObject, dataTypeWithSC, usera, namespace, "Production");

            ACCObject acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");
            ACCObject acc_association2 = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");

            bccp_to_append = coreComponentAPI.createRandomBCCP(currentReleaseObject, dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp_child = coreComponentAPI.createRandomASCCP(acc_association2, usera, namespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp_child, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);

            asccp_reuse = coreComponentAPI.createRandomASCCP(acc_association, usera, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp_reuse, "Production");
            coreComponentAPI.appendExtension(acc, usera, namespace, "Published");
            asccp_owner_usera = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp_reuse, usera, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(asccp_owner_usera.getDen(), current_release);
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_owner_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_owner_usera.getPropertyTerm() + "/" + asccp_reuse.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_owner_usera.getPropertyTerm() + "/" + asccp_reuse.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp_reuse.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        WebElement reusedASCCPNode = editBIEPage.getNodeByPath("/" + asccp_owner_usera.getPropertyTerm() + "/" + asccp_reuse.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(reusedASCCPNode);
        asbiePanel.setCardinalityMax(199);
        asbiePanel.setCardinalityMin(77);
        asbiePanel.setContextDefinition("aContextDefinition");
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        reusedASCCPNode = editBIEPage.getNodeByPath("/" + asccp_owner_usera.getPropertyTerm() + "/" + asccp_reuse.getPropertyTerm());
        asbiePanel = editBIEPage.getASBIEPanel(reusedASCCPNode);
        assertEquals("199", getText(asbiePanel.getCardinalityMaxField()));
        assertEquals("77", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("aContextDefinition", getText(asbiePanel.getContextDefinitionField()));
    }

    @Test
    public void end_user_can_reuse_developer_top_level_bie() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject acc, developer_acc, developer_acc_association;
        AppUserObject usera, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_association = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");
            ASCCObject ascc = coreComponentAPI.appendASCC(developer_acc_association, developer_asccp, "Published");
            developer_asccp_for_usera = coreComponentAPI.createRandomASCCP(developer_acc_association, developer, developerNamespace, "Published");
            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp, developer, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();

        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(developer_asccp_for_usera.getDen(), current_release);
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(developer_asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(developerBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());
    }

    @Test
    public void end_user_can_reuse_a_bie_that_has_a_nested_bie_reuse_all_reuse() {
        ASCCPObject developer_asccp, developer_asccp_for_usera, developer_asccp_lv2;
        ACCObject acc, developer_acc, developer_acc_association, developer_acc_association_lv2;
        AppUserObject usera, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, developerBIE_lv2;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_association = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_association_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_association_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            developer_asccp = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");
            ASCCObject ascc_lv1 = coreComponentAPI.appendASCC(developer_acc_association, developer_asccp, "Published");

            developer_asccp_for_usera = coreComponentAPI.createRandomASCCP(developer_acc_association, developer, developerNamespace, "Published");
            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp, developer, "WIP");
            developerBIE_lv2 = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "WIP");
        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();

        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(developer_asccp_for_usera.getDen(), current_release);
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(developer_asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(developerBIE_lv2);
        editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(developerBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());
    }

    @Test
    public void reuse_a_bie_only_the_asbie_details_remain_on_the_detail_pane_of_the() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject acc, developer_acc, developer_acc_association;
        AppUserObject usera, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_association = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");
            ASCCObject ascc = coreComponentAPI.appendASCC(developer_acc_association, developer_asccp, "Published");
            developer_asccp_for_usera = coreComponentAPI.createRandomASCCP(developer_acc_association, developer, developerNamespace, "Published");
            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp, developer, "WIP");
        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();

        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(developer_asccp_for_usera.getDen(), current_release);
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(developer_asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(developerBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        editBIEPage.clickOnDropDownMenuByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        click(getDriver().findElement(By.xpath("//span[contains(text(), \"Remove Reused BIE\")]//ancestor::button[1]")));
        click(getDriver().findElement(By.xpath("//span[contains(text(), \"Remove\")]//ancestor::button[1]")));
    }

    @Test
    public void end_user_cannot_discard_a_reused_bie_that_he_owns_if_it_is_used() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();

        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_usera.getDen(), current_release);
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        homePage.logout();
        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(useraBIE.getPropertyTerm());
        viewEditBIEPage.hitSearchButton();
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
        tr = viewEditBIEPage.getTableRecordByValue(useraBIE.getPropertyTerm());
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        click(viewEditBIEPage.getDiscardButton(true));

        WebElement discardButton = visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]"));
        assertTrue(!discardButton.isEnabled());

        String validationSummary = getText(visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-dialog-container//*[contains(@class, \"validation-summary\")]")));
        assertTrue(validationSummary.contains("This BIE cannot be discarded. Resolve the conflicting records to continue."));

        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Cancel\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Test
    public void end_user_cannot_move_a_reusing_bie_from_wip_state_to_qa_state_if() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + asccp_for_usera.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_usera.getDen(), current_release);
            bieExisting = true;

        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);

        click(viewEditBIEPage.getMoveToQA(true));
        assertBlockedStateTransition("QA");
    }

    @Test
    public void end_user_cannot_move_a_reusing_bie_from_qa_state_to_production_state_if() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "QA");
        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + asccp_for_usera.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_usera.getDen(), current_release);
            bieExisting = true;

        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        viewEditBIEPage.moveToQA();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        click(viewEditBIEPage.getMoveToProduction(true));
        assertBlockedStateTransition("Production");

    }

    @Test
    public void end_user_can_move_a_reusing_bie_from_wip_state_to_qa_state_if() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "QA");
        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + asccp_for_usera.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_usera.getDen(), current_release);
            bieExisting = true;

        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        viewEditBIEPage.moveToQA();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("QA", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    public void end_user_can_move_a_reusing_bie_from_qa_state_to_production_state_if() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "Production");
        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + asccp_for_usera.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_usera.getDen(), current_release);
            bieExisting = true;

        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        viewEditBIEPage.moveToQA();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        viewEditBIEPage.moveToProduction();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("Production", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    public void end_user_cannot_move_a_reused_bie_from_qa_state_to_wip_state_if() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "QA");
        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + asccp_for_usera.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_usera.getDen(), current_release);
            bieExisting = true;

        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        viewEditBIEPage.moveToQA();
        homePage.logout();
        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(useraBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        click(viewEditBIEPage.getBackToWIP(true));
        assertBlockedStateTransition("WIP");
    }

    @Test
    public void end_user_can_move_a_reused_bie_from_qa_state_to_wip_state_if() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "QA");
        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + asccp_for_usera.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_usera.getDen(), current_release);
            bieExisting = true;

        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        homePage.logout();
        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(useraBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        viewEditBIEPage.backToWIP();
        viewEditBIEPage.setDEN(useraBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("WIP", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    public void end_user_can_see_the_details_of_a_reused_bie_node_that_he_does() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(useraBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setRemark("useraBIE remark");
        topLevelASBIEPPanel.setContextDefinition("useraBIE definition");
        topLevelASBIEPPanel.setBusinessTerm("useraBIE business term");
        topLevelASBIEPPanel.setStatus("useraBIE status");
        editBIEPage.hitUpdateButton();

        homePage.logout();
        homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + asccp_for_usera.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_usera.getDen(), current_release);
            bieExisting = true;
        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        WebElement asccpNode = editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        EditBIEPage.ReusedASBIEPanel reusedASBIEPanel = editBIEPage.getReusedASBIEPanel(asccpNode);

        assertEquals("useraBIE remark", getText(reusedASBIEPanel.getRemarkField()));
        assertEquals("useraBIE business term", getText(reusedASBIEPanel.getLegacyBusinessTermField()));
        assertEquals("useraBIE status", getText(reusedASBIEPanel.getStatusField()));

        asccpNode = editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        asbiePanel.setCardinalityMax(199);
        asbiePanel.setCardinalityMin(77);
        asbiePanel.setContextDefinition("association of the Reused BIE");
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        asccpNode = editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        assertEquals("199", getText(asbiePanel.getCardinalityMaxField()));
        assertEquals("77", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("association of the Reused BIE", getText(asbiePanel.getContextDefinitionField()));
    }

    @Disabled("Blob-based Express BIE download is not reliably observable in the current browser automation path.")
    @Test
    public void end_user_can_express_a_reusing_bie_that_reuses_a_bie_in_wip_state() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(useraBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setRemark("useraBIE remark");
        topLevelASBIEPPanel.setContextDefinition("useraBIE definition");
        topLevelASBIEPPanel.setBusinessTerm("useraBIE business term");
        topLevelASBIEPPanel.setStatus("useraBIE status");
        editBIEPage.hitUpdateButton();

        homePage.logout();
        homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + asccp_for_usera.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_usera.getDen(), current_release);
            bieExisting = true;
        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        homePage.openPage();
        bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1200));
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(current_release, asccp_for_usera.getDen());
        expressBIEPage.selectXMLSchemaExpression();
        installBrowserDownloadCapture();
        click(expressBIEPage.getGenerateButton());
        String generatedFilename = waitForCapturedDownloadFilename();
        assertTrue(generatedFilename.endsWith(".xsd"));
    }

    @Test
    public void end_user_can_remove_reused_bie_references_at_any_level_even_if_theres_another() {
        ASCCPObject asccp, asccp_for_usera, asccp_lv2;
        BCCPObject bccp;
        ACCObject acc, acc_association, acc_lv2;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", current_release);
            bccp = coreComponentAPI.createRandomBCCP(currentReleaseObject, dataType, developer, developerNamespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            acc_lv2 = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            coreComponentAPI.appendASCC(acc_lv2, asccp, "Production");
            asccp_lv2 = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(acc_lv2, asccp_lv2, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_lv2, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
            userbBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp_for_usera, userb, "WIP");
        }

        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(userbBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNodeAndLevel("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm(), 2);
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.openPage();
        selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNodeAndLevel("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm(), 1);
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        editBIEPage.clickOnDropDownMenuByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        click(getDriver().findElement(By.xpath("//span[contains(text(), \"Remove Reused BIE\")]//ancestor::button[1]")));
        click(getDriver().findElement(By.xpath("//span[contains(text(), \"Remove\")]//ancestor::button[1]")));

        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());
    }

    @Test
    @Disabled("Not yet automated - documented case 24.1.15 'Enable global schema for reused BIE references'. " +
            "Was an empty test body reporting false-positive coverage; disabled until the global-schema toggle is automated.")
    public void enable_the_global_schema_for_reused_bie_references_no_matter_it_has_nested_reused() {
    }

    @Test
    public void retain_all_enabled_properties_under_the_reused_bie_hierarchy_when_the_user_clicks_the() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, euNamespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Production");
            asccp_for_usera = coreComponentAPI.createRandomASCCP(acc_association, usera, euNamespace, "Production");
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(useraBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setRemark("retained useraBIE remark");
        topLevelASBIEPPanel.setContextDefinition("retained useraBIE definition");
        topLevelASBIEPPanel.setBusinessTerm("retained useraBIE business term");
        topLevelASBIEPPanel.setStatus("retained useraBIE status");
        editBIEPage.hitUpdateButton();

        homePage.logout();
        homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + asccp_for_usera.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_usera.getDen(), current_release);
            bieExisting = true;
        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.retainReusedBIEOnNode("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(0, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.openPage();
        WebElement asccpNode = editBIEPage.getNodeByPath("/" + asccp_for_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        assertEquals("retained useraBIE remark", getText(asbiePanel.getRemarkField()));
    }
}
