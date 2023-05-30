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
import org.oagi.score.e2e.page.bie.CreateBIEForSelectTopLevelConceptPage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_24_1_ReuseBIE extends BaseTest {
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
    public void test_TA_24_1_1_a_and_b() {
        ASCCPObject asccp, asccp_owner_usera, asccp_to_append;
        BCCPObject bccp;
        ACCObject acc;
        AppUserObject usera;
        NamespaceObject namespace;
        BusinessContextObject context;
        TopLevelASBIEPObject useraBIE;
        String prev_release = "10.8.5";
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
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            ACCObject acc_association = coreComponentAPI.createRandomACC(usera, prevReleaseObject, namespace, "Production");
            ACCObject acc_association2 = coreComponentAPI.createRandomACC(usera, prevReleaseObject, namespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc_association, usera, namespace, "Production");
            asccp_to_append = coreComponentAPI.createRandomASCCP(acc_association, usera, namespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            coreComponentAPI.appendExtension(acc, usera, namespace, "Production");
            asccp_owner_usera = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(asccp_owner_usera.getDen(), prev_release);
        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_owner_usera.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp_owner_usera.getPropertyTerm() + "/Extension");
        SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp_owner_usera.getPropertyTerm() + " User Extension Group. Details");
        selectCCPropertyPage.selectAssociation(asccp_to_append.getDen());
        ACCExtensionViewEditPage.setNamespace(namespace);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        ACCExtensionViewEditPage.moveToProduction();
        bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIE);
        editBIEPage.clickOnDropDownMenuByPath("/" + asccp_owner_usera.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Reuse BIE\")]")).size());

        SelectProfileBIEToReuseDialog  selectProfileBIEToReuseDialog =editBIEPage.reuseBIEOnNode("/" + asccp_owner_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);

        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\""+asccp.getPropertyTerm()+"\"]//ancestor::div/mat-icon[@role=\"img\"][@data-mat-icon-name=\"fa-recycle\"]")).size());
        WebElement asccpNode = editBIEPage.getNodeByPath("/" + asccp_owner_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        asbiePanel.setCardinalityMax(199);
        asbiePanel.setCardinalityMin(77);
        asbiePanel.setContextDefinition("aContextDefinition");
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        asccpNode = editBIEPage.getNodeByPath("/" + asccp_owner_usera.getPropertyTerm()+ "/" + asccp.getPropertyTerm());
        asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        assertEquals("199", getText(asbiePanel.getCardinalityMaxField()));
        assertEquals("77", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("aContextDefinition", getText(asbiePanel.getContextDefinitionField()));
    }
    @Test
    public void test_TA_24_1_1_c_and_d() {


    }
    @Test
    public void test_TA_24_1_1_e() {

    }

    @Test
    public void test_TA_24_1_2() {

    }

    @Test
    public void test_TA_24_1_3() {

    }

    @Test
    public void test_TA_24_1_4_a() {

    }

    @Test
    public void test_TA_24_1_4_b() {

    }

    @Test
    public void test_TA_24_1_5() {

    }

    @Test
    public void test_TA_24_1_6() {

    }

    @Test
    public void test_TA_24_1_7() {

    }

    @Test
    public void test_TA_24_1_8() {

    }

    @Test
    public void test_TA_24_1_9() {

    }

    @Test
    public void test_TA_24_1_10() {

    }

    @Test
    public void test_TA_24_1_11() {

    }

    @Test
    public void test_TA_24_1_12() {

    }


    @Test
    public void test_TA_24_1_13() {

    }

    @Test
    public void test_TA_24_1_14() {

    }


    @Test
    public void test_TA_24_1_15() {

    }

    @Test
    public void test_TA_24_1_16() {

    }




}
