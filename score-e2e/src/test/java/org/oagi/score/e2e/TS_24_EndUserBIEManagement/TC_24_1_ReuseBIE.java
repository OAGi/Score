package org.oagi.score.e2e.TS_24_EndUserBIEManagement;

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
import org.oagi.score.e2e.page.MultiActionSnackBar;
import org.oagi.score.e2e.page.bie.*;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.File;
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

    @Test
    public void test_TA_24_1_1_a_and_b() {
        ASCCPObject asccp, asccp_owner_usera, asccp_to_append, asccp_child, asccp_reuse;
        BCCPObject bccp, bccp_to_append, bccp_child, bccp_not_reuse;
        ACCObject acc;
        AppUserObject usera;
        NamespaceObject namespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE;
        String prev_release = "10.8.6";
        ReleaseObject prevReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(prev_release);
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, prevReleaseObject, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", prevReleaseObject.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            bccp_child = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            DTObject dataTypeWithSC = coreComponentAPI.getBDTByGuidAndReleaseNum("3292eaa5630b48ecb7c4249b0ddc760e", prevReleaseObject.getReleaseNumber());
            bccp_not_reuse = coreComponentAPI.createRandomBCCP(dataTypeWithSC, usera, namespace, "Production");

            ACCObject acc_association = coreComponentAPI.createRandomACC(usera, prevReleaseObject, namespace, "Production");
            ACCObject acc_association2 = coreComponentAPI.createRandomACC(usera, prevReleaseObject, namespace, "Production");

            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
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

        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp_reuse.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());
    }

    @Test
    public void test_TA_24_1_1_c_and_d() {
        ASCCPObject asccp, asccp_owner_usera, asccp_to_append, asccp_child, asccp_reuse;
        BCCPObject bccp, bccp_to_append, bccp_child, bccp_not_reuse;
        ACCObject acc;
        AppUserObject usera;
        NamespaceObject namespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", current_release);
            bccp = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            bccp_child = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            DTObject dataTypeWithSC = coreComponentAPI.getBDTByGuidAndReleaseNum("3292eaa5630b48ecb7c4249b0ddc760e", current_release);
            bccp_not_reuse = coreComponentAPI.createRandomBCCP(dataTypeWithSC, usera, namespace, "Production");

            ACCObject acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");
            ACCObject acc_association2 = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");

            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
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
    public void test_TA_24_1_1_e() {
        ASCCPObject asccp_owner_usera, asccp_to_append, asccp_child, asccp_reuse;
        BCCPObject bccp, bccp_to_append, bccp_child, bccp_not_reuse;
        ACCObject acc;
        AppUserObject usera;
        NamespaceObject namespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", current_release);
            bccp = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            bccp_child = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            DTObject dataTypeWithSC = coreComponentAPI.getBDTByGuidAndReleaseNum("3292eaa5630b48ecb7c4249b0ddc760e", current_release);
            bccp_not_reuse = coreComponentAPI.createRandomBCCP(dataTypeWithSC, usera, namespace, "Production");

            ACCObject acc_association = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");
            ACCObject acc_association2 = coreComponentAPI.createRandomACC(usera, currentReleaseObject, namespace, "Production");

            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
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
    public void test_TA_24_1_2() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject acc, developer_acc, developer_acc_association;
        AppUserObject usera, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
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
    public void test_TA_24_1_3() {
        ASCCPObject developer_asccp, developer_asccp_for_usera, developer_asccp_lv2;
        ACCObject acc, developer_acc, developer_acc_association, developer_acc_association_lv2;
        AppUserObject usera, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE, developerBIE_lv2;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
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
    public void test_TA_24_1_4_a_and_b() {
        ASCCPObject developer_asccp, developer_asccp_for_usera;
        ACCObject acc, developer_acc, developer_acc_association;
        AppUserObject usera, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject developerBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
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
    public void test_TA_24_1_5() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

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
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]")));

        MultiActionSnackBar multiActionSnackBar = getMultiActionSnackBar(getDriver());
        assertEquals("Failed to discard BIE", getText(multiActionSnackBar.getMessageElement()));
    }

    @Test
    public void test_TA_24_1_6() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

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
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Failed to update BIE state"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//score-multi-actions-snack-bar//span[contains(text(), \"Close\")]//ancestor::button[1]")));
    }

    @Test
    public void test_TA_24_1_7() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

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
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Failed to update BIE state"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//score-multi-actions-snack-bar//span[contains(text(), \"Close\")]//ancestor::button[1]")));

    }

    @Test
    public void test_TA_24_1_8() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

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
    public void test_TA_24_1_9() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

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
    public void test_TA_24_1_10() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

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
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("Failed to update BIE state"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//score-multi-actions-snack-bar//span[contains(text(), \"Close\")]//ancestor::button[1]")));
    }

    @Test
    public void test_TA_24_1_11() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

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
        viewEditBIEPage.BackToWP();
        viewEditBIEPage.setDEN(useraBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("WIP", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    public void test_TA_24_1_12() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

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

    @Test
    public void test_TA_24_1_13() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

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
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(current_release, asccp_for_usera.getDen());
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
    public void test_TA_24_1_14() {
        ASCCPObject asccp, asccp_for_usera, asccp_lv2;
        BCCPObject bccp;
        ACCObject acc, acc_association, acc_lv2;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, currentReleaseObject, euNamespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", current_release);
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, developerNamespace, "WIP");
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
    public void test_TA_24_1_15() {
    }

    @Test
    public void test_TA_24_1_16() {
        ASCCPObject asccp, asccp_for_usera;
        ACCObject acc, acc_association;
        AppUserObject usera, userb, developer;
        NamespaceObject namespace, developerNamespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE, userbBIE;
        String current_release = "10.8.8";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(current_release);

        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        {
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

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
