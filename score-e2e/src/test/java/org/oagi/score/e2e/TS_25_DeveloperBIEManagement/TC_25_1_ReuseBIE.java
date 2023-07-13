package org.oagi.score.e2e.TS_25_DeveloperBIEManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

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
    public void test_TA_25_1_1() {
        ASCCPObject devx_asccp, devx_asccp_for_devy;
        ACCObject acc, devx_acc, devx_acc_association;
        AppUserObject usera, devx, devy;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject devxBIE_WIP, devxBIE_QA, devxBIE_Production, devyBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(devx);
            devy = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(devy);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

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
    public void test_TA_25_1_2_a_and_b() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject acc, developer_acc, developer_acc_association;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

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
        click(getDriver().findElement(By.xpath("//span[contains(text(),\"Remove Reused BIE\")]")));
        click(getDriver().findElement(By.xpath("//span[contains(text(),\"Remove\")]//ancestor::button[1]")));
    }

    @Test
    public void test_TA_25_1_3() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        BCCPObject bccp_indicator_type, bccp_code_type;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

            /**
             * The owner of the ASCCP is developer
             */
            // Indicator. Type
            DTObject dt_indicator = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", current_release);
            bccp_indicator_type = coreComponentAPI.createRandomBCCP(dt_indicator, developer, developerNamespace, "Published");
            bccp_indicator_type.setNillable(false);
            coreComponentAPI.updateBCCP(bccp_indicator_type);

            DTObject dt_code = coreComponentAPI.getBDTByGuidAndReleaseNum("10ef56a1365842dc856007ca9fcc9021", current_release);
            bccp_code_type = coreComponentAPI.createRandomBCCP(dt_code, developer, developerNamespace, "Published");
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
    public void test_TA_25_1_4() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject developer_acc, developer_acc_association;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

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

        click(elementToBeClickable(getDriver(), By.xpath("//span[.=\"" + developer_asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")));
        ArrayList<String> tabs = new ArrayList<>(getDriver().getWindowHandles());
        getDriver().switchTo().window(tabs.get(1));

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        assertEquals(developer_asccp.getPropertyTerm(), editBIEPage.getTitle());
    }

    @Test
    public void test_TA_25_1_5() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject developer_acc, developer_acc_association;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

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

        click(elementToBeClickable(getDriver(), By.xpath("//span[.=\"" + developer_asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")));
        ArrayList<String> tabs = new ArrayList<>(getDriver().getWindowHandles());
        getDriver().switchTo().window(tabs.get(1));

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        assertEquals(developer_asccp.getPropertyTerm(), editBIEPage.getTitle());

        WebElement reusedASCCPNode = editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(reusedASCCPNode);
        asbiePanel.setCardinalityMax(199);
        asbiePanel.setCardinalityMin(77);
        asbiePanel.setContextDefinition("aContextDefinition");
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        reusedASCCPNode = editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        asbiePanel = editBIEPage.getASBIEPanel(reusedASCCPNode);
        assertEquals("199", getText(asbiePanel.getCardinalityMaxField()));
        assertEquals("77", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("aContextDefinition", getText(asbiePanel.getContextDefinitionField()));
    }

    @Test
    public void test_TA_25_1_6() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject developer_acc, developer_acc_association;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

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

        asccpNode = editBIEPage.getNodeByPath("/" + developer_asccp_for_usera.getPropertyTerm() + "/" + developer_asccp.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        assertDisabled(asbiePanel.getCardinalityMaxField());
        assertDisabled(asbiePanel.getCardinalityMinField());
        assertDisabled(asbiePanel.getContextDefinitionField());
    }

    @Test
    public void test_TA_25_1_7() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        BCCPObject bccp_indicator_type, bccp_code_type;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

            // Indicator. Type
            DTObject dt_indicator = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", current_release);
            bccp_indicator_type = coreComponentAPI.createRandomBCCP(dt_indicator, developer, developerNamespace, "Published");
            bccp_indicator_type.setNillable(false);
            coreComponentAPI.updateBCCP(bccp_indicator_type);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            BCCObject bcc_indicator = coreComponentAPI.appendBCC(developer_acc, bccp_indicator_type, "Published");;
            bcc_indicator.setCardinalityMax(199);
            bcc_indicator.setCardinalityMin(77);
            bcc_indicator.setDefinition("BIE Copy will keep the definition");
            coreComponentAPI.updateBCCP(bccp_indicator_type);
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        escape(getDriver());

        homePage.logout();
        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        bieMenu = homePage.getBIEMenu();
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage = bieMenu.openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage = copyBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        copyBIEForSelectBIEPage.setDEN(developerBIE.getDen());
        copyBIEForSelectBIEPage.hitSearchButton();
        copyBIEForSelectBIEPage.copyBIE(developer_asccp_root.getDen(), current_release);

        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setDEN(developerBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        editBIEPage.openPage();
        WebElement BCCPNode = editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + bccp_indicator_type.getPropertyTerm());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(BCCPNode);
        assertEquals("199", getText(bbiePanel.getCardinalityMaxField()));
        assertEquals("77", getText(bbiePanel.getCardinalityMinField()));
        assertEquals("BIE Copy will keep the definition", getText(bbiePanel.getContextDefinitionField()));
    }

    @Test
    public void test_TA_25_1_8() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        BCCPObject bccp_indicator_type;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

            // Indicator. Type
            DTObject dt_indicator = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", current_release);
            bccp_indicator_type = coreComponentAPI.createRandomBCCP(dt_indicator, developer, developerNamespace, "Published");
            bccp_indicator_type.setNillable(false);
            coreComponentAPI.updateBCCP(bccp_indicator_type);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            BCCObject bcc_indicator = coreComponentAPI.appendBCC(developer_acc, bccp_indicator_type, "Published");;
            bcc_indicator.setCardinalityMax(199);
            bcc_indicator.setCardinalityMin(77);
            bcc_indicator.setDefinition("BIE Copy will keep the definition");
            coreComponentAPI.updateBCCP(bccp_indicator_type);
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        escape(getDriver());

        homePage.logout();
        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setDEN(developerBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(reusedBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        click(elementToBeClickable(getDriver(), By.xpath("//mat-icon[contains(text(), \"more_vert\")]")));
        click(elementToBeClickable(getDriver(), By.xpath("//*[contains(text(),\"Find Reuses\")]//ancestor::li")));
        assertTrue(getDriver().findElement(By.xpath("//mat-dialog-content//a[contains(text(),\"" + developer_asccp_root.getPropertyTerm() + "\")]//ancestor::tr/td[1]//label/span[1]")).isDisplayed());

        //Check ReUse Report
        ReuseReportPage reuseReportPage = bieMenu.openReuseReportSubMenu();
        click(elementToBeClickable(getDriver(), By.xpath("//tr/td[3]//*[contains(text(),\""+developer_asccp_root.getPropertyTerm()+"\")]")));
        ArrayList<String> tabs = new ArrayList<>(getDriver().getWindowHandles());
        getDriver().switchTo().window(tabs.get(1));

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        assertEquals(developer_asccp_root.getPropertyTerm(), editBIEPage.getTitle());
        assertTrue(editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm()).isDisplayed());
    }

    @Test
    public void test_TA_25_1_9() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        BCCPObject bccp_indicator_type;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

            // Indicator. Type
            DTObject dt_indicator = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", current_release);
            bccp_indicator_type = coreComponentAPI.createRandomBCCP(dt_indicator, developer, developerNamespace, "Published");
            bccp_indicator_type.setNillable(false);
            coreComponentAPI.updateBCCP(bccp_indicator_type);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            BCCObject bcc_indicator = coreComponentAPI.appendBCC(developer_acc, bccp_indicator_type, "Published");;
            bcc_indicator.setCardinalityMax(199);
            bcc_indicator.setCardinalityMin(77);
            bcc_indicator.setDefinition("BIE Copy will keep the definition");
            coreComponentAPI.updateBCCP(bccp_indicator_type);
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        escape(getDriver());

        homePage.logout();
        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setDEN(developerBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        editBIEPage.getNodeByPath("/" + developer_asccp_root.getPropertyTerm() + "/" + developer_asccp_lv2.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + developer_asccp_lv2.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        click(elementToBeClickable(getDriver(), By.xpath("//mat-icon[contains(text(), \"more_vert\")]//ancestor::button[1]")));
        click(viewEditBIEPage.getDiscardButton(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]")));

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Failed to discard BIE"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));
    }

    @Test
    public void test_TA_25_1_10() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        BCCPObject bccp_indicator_type;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

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
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "WIP");
        }
        HomePage homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
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

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Failed to update BIE state"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));
    }

    @Test
    public void test_TA_25_1_11() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "QA");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "QA");
        }
        HomePage homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
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
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);

        click(viewEditBIEPage.getMoveToProduction(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Failed to update BIE state"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));
    }

    @Test
    public void test_TA_25_1_12() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

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
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "QA");
        }
        HomePage homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
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
    public void test_TA_25_1_13() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

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
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "Production");
        }
        HomePage homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
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
    public void test_TA_25_1_14() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

            /**
             * The owner of the ASCCP is developer
             */
            developer_acc = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_acc_lv2 = coreComponentAPI.createRandomACC(developer, currentReleaseObject, developerNamespace, "Published");
            developer_asccp_lv2 = coreComponentAPI.createRandomASCCP(developer_acc_lv2, developer, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(developer_acc, developer_asccp_lv2, "Published");
            coreComponentAPI.appendExtension(developer_acc_lv2, developer, developerNamespace, "Published");
            developer_asccp_root = coreComponentAPI.createRandomASCCP(developer_acc, developer, developerNamespace, "Published");

            developerBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "QA");
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "QA");
        }
        HomePage homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
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

        homePage.logout();
        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
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

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Failed to update BIE state"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));

    }
    @Test
    public void test_TA_25_1_15() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

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
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "QA");
        }
        HomePage homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
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

        homePage.logout();
        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(reusedBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        click(td);
        click(viewEditBIEPage.getBackToWIP(true));
        viewEditBIEPage.setDEN(reusedBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("WIP", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    public void test_TA_25_1_16() {
        ASCCPObject developer_asccp_root, developer_asccp_lv2;
        ACCObject developer_acc, developer_acc_lv2;
        AppUserObject anotherDeveloper, developer;
        NamespaceObject developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, reusedBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

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
            reusedBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), developer_asccp_root, developer, "QA");
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

        homePage.logout();
        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        Boolean bieExisting = true;
        viewEditBIEPage.setDEN(developer_asccp_root.getDen());
        viewEditBIEPage.hitSearchButton();
        bieExisting = 0 < getDriver().findElements(By.xpath("//*[contains(text(),\"" + developer_asccp_root.getDen() + "\")]//ancestor::tr")).size();

        viewEditBIEPage.openPage();
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

    @Test
    public void test_TA_25_1_17() {

    }

    @Test
    public void test_TA_25_1_18() {

    }

    @Test
    public void test_TA_25_1_19() {

    }

    @Test
    public void test_TA_25_1_20() {

    }


}
