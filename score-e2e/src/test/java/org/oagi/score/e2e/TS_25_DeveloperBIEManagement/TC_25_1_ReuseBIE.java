package org.oagi.score.e2e.TS_25_DeveloperBIEManagement;

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
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.numberOfWindowsToBe;

@Execution(ExecutionMode.CONCURRENT)
public class TC_25_1_ReuseBIE extends BaseTest {
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
    public void developer_can_reuse_a_bie_the_reused_bie_can_be_in_any_state_and_owned_by() {
        ASCCPObject devx_asccp, devx_asccp_for_devy;
        ACCObject acc, devx_acc, devx_acc_association;
        AppUserObject usera, devx, devy;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject devxBIE_WIP, devxBIE_QA, devxBIE_Production, devyBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(devx);
            devy = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(devy);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx, library);

            /**
             * The owner of the ASCCP is developer
             */
            devx_acc = coreComponentAPI.createRandomACC(devx, currentReleaseObject, developerNamespace, "Published");
            devx_acc_association = coreComponentAPI.createRandomACC(devx, currentReleaseObject, developerNamespace, "Published");
            devx_asccp = coreComponentAPI.createRandomASCCP(devx_acc, devx, developerNamespace, "Published");
            ASCCObject ascc = coreComponentAPI.appendASCC(devx_acc_association, devx_asccp, "Published");
            devx_asccp_for_devy = coreComponentAPI.createRandomASCCP(devx_acc_association, devx, developerNamespace, "Published");
            devxBIE_WIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), devx_asccp, devx, "WIP");
        }
        HomePage homePage = loginPage().signIn(devy.getLoginId(), devy.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();

        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(devx_asccp_for_devy.getDen(), current_release);
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(devx_asccp_for_devy.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + devx_asccp_for_devy.getPropertyTerm() + "/" + devx_asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(devxBIE_WIP);
        editBIEPage.getNodeByPath("/" + devx_asccp_for_devy.getPropertyTerm() + "/" + devx_asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + devx_asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());
    }

    @Test
    public void only_the_asbie_details_remain_on_the_detail_pane_of_the_node() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject acc, developer_acc, developer_acc_association;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
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
        HomePage homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
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
    public void reuse_target_node_can_only_be_asbie_asbiep_abie_node_but_not_any_bbie_bbiep_nor() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        BCCPObject bccp_indicator_type, bccp_code_type;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            // Indicator. Type
            DTObject dt_indicator = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", current_release);
            bccp_indicator_type = coreComponentAPI.createRandomBCCP(currentReleaseObject, dt_indicator, developer, developerNamespace, "Published");
            bccp_indicator_type.setNillable(false);
            coreComponentAPI.updateBCCP(bccp_indicator_type);

            DTObject dt_code = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "8954f848e8f448c0a72785acd5a3a805", current_release);
            bccp_code_type = coreComponentAPI.createRandomBCCP(currentReleaseObject, dt_code, developer, developerNamespace, "Published");
            bccp_code_type.setNillable(false);
            coreComponentAPI.updateBCCP(bccp_code_type);

            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            BCCObject bcc_indicator = coreComponentAPI.appendBCC(developer_acc, bccp_indicator_type, "Published");
            BCCObject bcc_code = coreComponentAPI.appendBCC(developer_acc, bccp_code_type, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertTrue(selectProfileBIEToReuseDialog.isOpened());
        escape(getDriver());

        editBIEPage.clickOnDropDownMenuByPath("/" + developer_asccp_root.getPropertyTerm());
        assertThrows(TimeoutException.class, () -> {
            visibilityOfElementLocated(getDriver(), By.xpath("//span[contains(text(), \"Reuse BIE\")]"));
        });
        escape(getDriver());

        editBIEPage.clickOnDropDownMenuByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + bccp_code_type.getPropertyTerm());
        assertThrows(TimeoutException.class, () -> {
            visibilityOfElementLocated(getDriver(), By.xpath("//span[contains(text(), \"Reuse BIE\")]"));
        });
        escape(getDriver());

        editBIEPage.clickOnDropDownMenuByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + bccp_indicator_type.getPropertyTerm());
        assertThrows(TimeoutException.class, () -> {
            visibilityOfElementLocated(getDriver(), By.xpath("//span[contains(text(), \"Reuse BIE\")]"));
        });
    }

    @Test
    public void developer_can_click_on_the_bie_reuse_node_to_view_the_details_of_the_reused_bie() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject developer_acc, developer_acc_association;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();

        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(developer_asccp_for_usera.getDen(), current_release);
        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(developerBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        String currentTab = getDriver().getWindowHandle();
        retry(() -> {
            click(elementToBeClickable(getDriver(), By.xpath("//span[.=\"" + developer_asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon[@mattooltip=\"Reused\"]")));
            defaultWait(getDriver()).until(numberOfWindowsToBe(2));
        });
        // Loop through until we find a new window handle
        for (String windowHandle : getDriver().getWindowHandles()) {
            if (!currentTab.contentEquals(windowHandle)) {
                getDriver().switchTo().window(windowHandle);
                break;
            }
        }

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(topLevelASBIEP.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        assertEquals(developer_asccp.getPropertyTerm(), getText(editBIEPage.getTitle()));
    }

    @Test
    public void on_the_detail_pane_of_a_reused_bie_node_business_term_context_definition_remark_version_owner() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject developer_acc, developer_acc_association;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject reuseTopLevelASBIEP, topLevelASBIEP;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
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

            reuseTopLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                    Collections.singletonList(context), developer_asccp, developer, "WIP");
            topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                    Collections.singletonList(context), developer_asccp_for_usera, developer, "WIP");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(reuseTopLevelASBIEP);
        WebElement reusedASCCPNode = editBIEPage.getNodeByPath("/" + developer_asccp.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(reusedASCCPNode);
        asbiePanel.setRemark("aRemark");
        editBIEPage.hitUpdateButton();

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        String reusedAsccpPath = "/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm();
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode(reusedAsccpPath);
        selectProfileBIEToReuseDialog.selectBIEToReuse(reuseTopLevelASBIEP);

        editBIEPage.openPage();
        reusedASCCPNode = editBIEPage.getNodeByPath(reusedAsccpPath);
        asbiePanel = editBIEPage.getASBIEPanel(reusedASCCPNode);
        assertEquals("aRemark", getText(asbiePanel.getRemarkField()));
    }

    @Test
    public void developer_can_view_the_details_of_the_nodes_of_a_bie_reuse_node_but_it_cannot() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject developer_acc, developer_acc_association;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
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

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developerBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setRemark("developerBIE remark");
        topLevelASBIEPPanel.setContextDefinition("developerBIE definition");
        topLevelASBIEPPanel.setBusinessTerm("developerBIE business term");
        topLevelASBIEPPanel.setStatus("developerBIE status");
        editBIEPage.hitUpdateButton();

        homePage.logout();
        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(developer_asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + developer_asccp_for_usera.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(developer_asccp_for_usera.getDen(), current_release);
            bieExisting = true;
        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(developer_asccp_for_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(developerBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        WebElement asccpNode = editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        EditBIEPage.ReusedASBIEPanel reusedASBIEPanel = editBIEPage.getReusedASBIEPanel(asccpNode);

        assertEquals("developerBIE remark", getText(reusedASBIEPanel.getRemarkField()));
        assertEquals("developerBIE business term", getText(reusedASBIEPanel.getLegacyBusinessTermField()));
        assertEquals("developerBIE status", getText(reusedASBIEPanel.getStatusField()));
        assertDisabled(reusedASBIEPanel.getRemarkField());
        assertDisabled(reusedASBIEPanel.getLegacyBusinessTermField());
        assertDisabled(reusedASBIEPanel.getStatusField());
    }

    @Test
    public void developer_can_copy_a_reusing_bie_that_has_a_bie_reuse_node_in_this_case_the() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        BCCPObject bccp_indicator_type, bccp_code_type;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            // Indicator. Type
            DTObject dt_indicator = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", current_release);
            bccp_indicator_type = coreComponentAPI.createRandomBCCP(currentReleaseObject, dt_indicator, developer, developerNamespace, "Published");
            bccp_indicator_type.setNillable(false);
            bccp_indicator_type.setDefinition("BCCP definition");
            bccp_indicator_type.setDefinitionSource("BCCP definition source");
            bccp_indicator_type.setDefaultValue("111");
            coreComponentAPI.updateBCCP(bccp_indicator_type);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            BCCObject bcc_indicator = coreComponentAPI.appendBCC(developer_acc, bccp_indicator_type, "Published");
            ;
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "WIP");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        escape(getDriver());

        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage = bieMenu.openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage = copyBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        copyBIEForSelectBIEPage.setDEN(developerBIE.getDen());
        copyBIEForSelectBIEPage.hitSearchButton();
        copyBIEForSelectBIEPage.copyBIE(developerBIE.getDen(), current_release);

        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developerBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.openPage();
        WebElement BCCPNode = editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + bccp_indicator_type.getPropertyTerm());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(BCCPNode);
        assertEquals("111", getText(bbiePanel.getDefaultValueField()));
    }

    @Test
    public void developer_can_view_all_the_reuses_of_all_bie_in_the_reuse_bie_report_page_when() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        BCCPObject bccp_indicator_type;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            // Indicator. Type
            DTObject dt_indicator = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", current_release);
            bccp_indicator_type = coreComponentAPI.createRandomBCCP(currentReleaseObject, dt_indicator, developer, developerNamespace, "Published");
            bccp_indicator_type.setNillable(false);
            coreComponentAPI.updateBCCP(bccp_indicator_type);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            BCCObject bcc_indicator = coreComponentAPI.appendBCC(developer_acc, bccp_indicator_type, "Published");
            ;
            bcc_indicator.setCardinalityMax(199);
            bcc_indicator.setCardinalityMin(77);
            bcc_indicator.setDefinition("BIE Copy will keep the definition");
            coreComponentAPI.updateBCCP(bccp_indicator_type);
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "WIP");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        escape(getDriver());

        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developerBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(reusedBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        click(elementToBeClickable(getDriver(), By.xpath("//mat-icon[contains(text(), \"more_vert\")]//ancestor::button[1]")));
        click(elementToBeClickable(getDriver(), By.xpath("//span[contains(text(), \"Find Reuses\")]//ancestor::button[1]")));
        waitFor(Duration.ofMillis(1000L));
        assertTrue(getDriver().findElement(By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//a[contains(text(), \"" + developer_asccp_root.getPropertyTerm() + "\")]//ancestor::tr/td[1]")).isDisplayed());
        escape(getDriver());

        //Check ReUse Report
        ReuseReportPage reuseReportPage = bieMenu.openReuseReportSubMenu();
        click(elementToBeClickable(getDriver(), By.xpath("//tr/td[3]//*[contains(text(),\"" + developer_asccp_root.getPropertyTerm() + "\")]")));
        ArrayList<String> tabs = new ArrayList<>(getDriver().getWindowHandles());
        getDriver().switchTo().window(tabs.get(1));

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1, currentUrl.indexOf("?")));

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        assertEquals(developer_asccp_root.getPropertyTerm(), getText(editBIEPage.getTitle()));
        assertTrue(editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm()).isDisplayed());
    }

    @Test
    public void developer_cannot_discard_a_reused_bie_that_he_owned_if_it_is_used_in_another_top() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        BCCPObject bccp_indicator_type;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            // Indicator. Type
            DTObject dt_indicator = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", current_release);
            bccp_indicator_type = coreComponentAPI.createRandomBCCP(currentReleaseObject, dt_indicator, developer, developerNamespace, "Published");
            bccp_indicator_type.setNillable(false);
            coreComponentAPI.updateBCCP(bccp_indicator_type);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            BCCObject bcc_indicator = coreComponentAPI.appendBCC(developer_acc, bccp_indicator_type, "Published");
            ;
            bcc_indicator.setCardinalityMax(199);
            bcc_indicator.setCardinalityMin(77);
            bcc_indicator.setDefinition("BIE Copy will keep the definition");
            coreComponentAPI.updateBCCP(bccp_indicator_type);
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "WIP");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        escape(getDriver());

        homePage.logout();
        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developerBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(reusedBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        click(viewEditBIEPage.getDiscardButton(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]")));

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Failed to discard BIE"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//score-multi-actions-snack-bar//span[contains(text(), \"Close\")]//ancestor::button[1]")));
    }

    @Test
    public void developer_can_move_a_reusing_bie_from_wip_state_to_qa_state_even_if_the_reused() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        BCCPObject bccp_indicator_type;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "WIP");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + developer_asccp_root.getDen() + "\")]//ancestor::tr")).size();

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);

        click(viewEditBIEPage.getMoveToQA(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));

        String snackBarMessage = getSnackBarMessage(getDriver());
        assertEquals("Updated", snackBarMessage);
    }

    @Test
    public void developer_can_move_a_reusing_bie_from_qa_state_to_production_state_even_if_the_reused() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "QA");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + developer_asccp_root.getDen() + "\")]//ancestor::tr")).size();

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);

        viewEditBIEPage.moveToQA();
        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        click(viewEditBIEPage.getMoveToProduction(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));

        String snackBarMessage = getSnackBarMessage(getDriver());
        assertEquals("Updated", snackBarMessage);
    }

    @Test
    public void developer_can_move_a_reusing_bie_from_wip_state_to_qa_state_if_the_reused_bie() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "QA");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + developer_asccp_root.getDen() + "\")]//ancestor::tr")).size();

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);

        viewEditBIEPage.moveToQA();
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("QA", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    public void developer_can_move_a_reusing_bie_from_qa_state_to_production_state_if_the_reused_bie() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "Production");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + developer_asccp_root.getDen() + "\")]//ancestor::tr")).size();

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);

        viewEditBIEPage.moveToQA();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        viewEditBIEPage.moveToProduction();
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("Production", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    public void developer_can_move_a_reused_bie_from_qa_state_to_wip_state_even_if_the_reusing() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "QA");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + developer_asccp_root.getDen() + "\")]//ancestor::tr")).size();

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developerBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        viewEditBIEPage.moveToQA();
        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(reusedBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        click(viewEditBIEPage.getBackToWIP(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));

        String snackBarMessage = getSnackBarMessage(getDriver());
        assertEquals("Updated", snackBarMessage);
    }

    @Test
    public void developer_can_move_a_reused_bie_from_qa_state_to_wip_state_if_the_reusing_bie() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "QA");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + developer_asccp_root.getDen() + "\")]//ancestor::tr")).size();

        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(reusedBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        click(viewEditBIEPage.getBackToWIP(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
        viewEditBIEPage.setDEN(reusedBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("WIP", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    public void developer_can_see_the_details_of_a_reused_bie_node_that_he_does_not_own_and() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "WIP");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(reusedBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setRemark("developerBIE remark");
        topLevelASBIEPPanel.setContextDefinition("developerBIE definition");
        topLevelASBIEPPanel.setBusinessTerm("developerBIE business term");
        topLevelASBIEPPanel.setStatus("developerBIE status");
        editBIEPage.hitUpdateButton();
        editBIEPage.moveToQA();

        viewEditBIEPage.openPage();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + developer_asccp_root.getDen() + "\")]//ancestor::tr")).size();

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        WebElement asccpNode = editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        EditBIEPage.ReusedASBIEPanel reusedASBIEPanel = editBIEPage.getReusedASBIEPanel(asccpNode);

        assertEquals("developerBIE remark", getText(reusedASBIEPanel.getRemarkField()));
        assertEquals("developerBIE business term", getText(reusedASBIEPanel.getLegacyBusinessTermField()));
        assertEquals("developerBIE status", getText(reusedASBIEPanel.getStatusField()));

        asccpNode = editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        asbiePanel.setCardinalityMax(199);
        asbiePanel.setCardinalityMin(77);
        asbiePanel.setContextDefinition("association of the Reused BIE");
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        asccpNode = editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        assertEquals("199", getText(asbiePanel.getCardinalityMaxField()));
        assertEquals("77", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("association of the Reused BIE", getText(asbiePanel.getContextDefinitionField()));
    }

    @Disabled("Blob-based Express BIE download is not reliably observable in the current browser automation path.")
    @Test
    public void developer_can_express_a_reusing_bie_that_reuses_a_bie_in_wip_state_and_owned_by() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_lv2, developer, "WIP");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(reusedBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setRemark("developerBIE remark");
        topLevelASBIEPPanel.setContextDefinition("developerBIE definition");
        topLevelASBIEPPanel.setBusinessTerm("developerBIE business term");
        topLevelASBIEPPanel.setStatus("developerBIE status");
        editBIEPage.hitUpdateButton();
        editBIEPage.moveToQA();

        viewEditBIEPage.openPage();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + developer_asccp_root.getDen() + "\")]//ancestor::tr")).size();

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        homePage.openPage();
        bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(current_release, developer_asccp_root.getDen());
        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }

    }

    @Test
    public void developer_can_remove_reused_bie_references_at_any_level_even_if_theres_another_same_reused_bie() {
        ASCCPObject asccp, asccp_for_devx, asccp_lv2;
        BCCPObject bccp;
        ACCObject acc, acc_association, acc_lv2;
        AppUserObject devx, devy, usera;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject devxBIE, devyBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(devx);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(devx);
        {
            devy = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(devy);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);

            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is deva
             */
            acc = coreComponentAPI.createRandomACC(devx, currentReleaseObject, developerNamespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", current_release);
            bccp = coreComponentAPI.createRandomBCCP(currentReleaseObject, dataType, devx, developerNamespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            acc_lv2 = coreComponentAPI.createRandomACC(devx, currentReleaseObject, developerNamespace, "Published");
            acc_association = coreComponentAPI.createRandomACC(devx, currentReleaseObject, developerNamespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, devx, developerNamespace, "Published");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Published");
            coreComponentAPI.appendASCC(acc_lv2, asccp, "Published");
            asccp_lv2 = coreComponentAPI.createRandomASCCP(acc_association, devx, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(acc_lv2, asccp_lv2, "Published");
            asccp_for_devx = coreComponentAPI.createRandomASCCP(acc_lv2, devx, developerNamespace, "Published");
            devxBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, devx, "WIP");
            devyBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp_for_devx, devy, "WIP");
        }

        HomePage homePage = loginPage().signIn(devy.getLoginId(), devy.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(devyBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNodeAndLevel("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm(), 2);
        selectProfileBIEToReuseDialog.selectBIEToReuse(devxBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.openPage();
        selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNodeAndLevel("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp.getPropertyTerm(), 1);
        selectProfileBIEToReuseDialog.selectBIEToReuse(devxBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.getNodeByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        editBIEPage.clickOnDropDownMenuByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        click(getDriver().findElement(By.xpath("//span[contains(text(), \"Remove Reused BIE\")]//ancestor::button[1]")));
        click(getDriver().findElement(By.xpath("//span[contains(text(), \"Remove\")]//ancestor::button[1]")));

        editBIEPage.getNodeByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());
    }

    @Test
    public void enable_the_global_schema_for_reused_bie_references_no_matter_it_has_nested_reused_bie_or() {


    }

    @Test
    public void retain_all_enabled_properties_under_the_reused_bie_hierarchy_when_the_user_clicks_the_retain() {
        ASCCPObject asccp, asccp_for_devx;
        ACCObject acc, acc_association;
        AppUserObject devx, devy;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject devxBIE, devyBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, current_release);

        devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(devx);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(devx);
        {
            devy = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(devy);

            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is devx
             */
            acc = coreComponentAPI.createRandomACC(devx, currentReleaseObject, developerNamespace, "Published");
            acc_association = coreComponentAPI.createRandomACC(devx, currentReleaseObject, developerNamespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, devx, developerNamespace, "Published");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp, "Published");
            asccp_for_devx = coreComponentAPI.createRandomASCCP(acc_association, devx, developerNamespace, "Published");
            devxBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, devx, "WIP");
        }
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(devxBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setRemark("retained devxBIE remark");
        topLevelASBIEPPanel.setContextDefinition("retained devxBIE definition");
        topLevelASBIEPPanel.setBusinessTerm("retained devxBIE business term");
        topLevelASBIEPPanel.setStatus("retained devxBIE status");
        editBIEPage.hitUpdateButton();

        homePage.logout();
        homePage = loginPage().signIn(devy.getLoginId(), devy.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(asccp_for_devx.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + asccp_for_devx.getDen() + "\")]//ancestor::tr")).size();
        if (!bieExisting) {
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_for_devx.getDen(), current_release);
            bieExisting = true;
        }
        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(asccp_for_devx.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(devxBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.retainReusedBIEOnNode("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        editBIEPage.getNodeByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(0, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.openPage();
        WebElement asccpNode = editBIEPage.getNodeByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        assertEquals("retained devxBIE remark", getText(asbiePanel.getRemarkField()));
    }


}
