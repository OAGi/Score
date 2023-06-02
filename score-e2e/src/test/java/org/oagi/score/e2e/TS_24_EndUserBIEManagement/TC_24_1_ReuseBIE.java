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
import static org.oagi.score.e2e.impl.PageHelper.*;

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
        ASCCPObject asccp, asccp_owner_usera, asccp_to_append,asccp_child, asccp_reuse;
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
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp_reuse, usera, "WIP");
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
        SelectProfileBIEToReuseDialog  selectProfileBIEToReuseDialog =editBIEPage.reuseBIEOnNode("/" + asccp_owner_usera.getPropertyTerm() + "/Extension/" + asccp_reuse.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(useraBIE);

        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\""+asccp_reuse.getPropertyTerm()+"\"]//ancestor::div[1]/fa-icon")).size());
        WebElement asccpNode = editBIEPage.getNodeByPath("/" + asccp_owner_usera.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        asbiePanel.toggleUsed();
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
        AppUserObject usera;
        NamespaceObject namespace;
        BusinessContextObject context;
        String useraASCCP = "Cancel Acknowledge Employee Work Time. Cancel Acknowledge Employee Work Time";
        String BIEDocumentReference = "Document Reference. Document Reference";
        String BIEWorkTimePeriod = "Work Time Period. Time Period";
        String BIEWorkLocation = "Work Location. Location";
        String BIEStateChange = "State Change. State Change";
        String BIEPersonName = "Person Name. Person Name";
        String BIELineIdentifierSet = "Line Identifier Set. Identifier Set";
        String BIECommonTimeReporting = "Common Time Reporting. Common Time Reporting";
        String BIEEmployeeWorkTime = "Employee Work Time. Employee Work Time";
        String BIEResponseCriteria = "Response Criteria. Response Action Criteria";
        ReleaseObject currentReleaseObject = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(useraASCCP, currentReleaseObject.getReleaseNumber());
        viewEditBIEPage.openPage();
        createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(BIEDocumentReference, currentReleaseObject.getReleaseNumber());
        viewEditBIEPage.openPage();
        createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(BIEWorkTimePeriod, currentReleaseObject.getReleaseNumber());
        viewEditBIEPage.openPage();
        createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(BIEWorkLocation, currentReleaseObject.getReleaseNumber());
        viewEditBIEPage.openPage();
        createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(BIEStateChange, currentReleaseObject.getReleaseNumber());
        viewEditBIEPage.openPage();
        createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(BIEPersonName, currentReleaseObject.getReleaseNumber());
        viewEditBIEPage.openPage();
        createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(BIELineIdentifierSet, currentReleaseObject.getReleaseNumber());
        viewEditBIEPage.openPage();
        createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(BIECommonTimeReporting, currentReleaseObject.getReleaseNumber());

        viewEditBIEPage.openPage();
        createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(BIEEmployeeWorkTime, currentReleaseObject.getReleaseNumber());
        viewEditBIEPage.openPage();
        createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.createBIE(BIEResponseCriteria, currentReleaseObject.getReleaseNumber());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(useraASCCP);
        viewEditBIEPage.hitSearchButton();
        WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(tr);

        SelectProfileBIEToReuseDialog  selectProfileBIEToReuseDialog =editBIEPage.reuseBIEOnNode("/Cancel Acknowledge Employee Work Time/Data Area/Employee Work Time/Document Reference");
        selectProfileBIEToReuseDialog.selectBIEToReuse(BIEDocumentReference);

        editBIEPage.getNodeByPath("/Cancel Acknowledge Employee Work Time/Data Area/Employee Work Time/Document Reference");
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"Document Reference\"]//ancestor::div/mat-icon[@role=\"img\"][@data-mat-icon-name=\"fa-recycle\"]")).size());

        selectProfileBIEToReuseDialog =editBIEPage.reuseBIEOnNode("/Cancel Acknowledge Employee Work Time/Data Area/Employee Work Time/Common Time Reporting/Work Time Period");
        selectProfileBIEToReuseDialog.selectBIEToReuse(BIEWorkTimePeriod);

        editBIEPage.getNodeByPath("/Cancel Acknowledge Employee Work Time/Data Area/Employee Work Time/Common Time Reporting/Work Time Period");
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"Work Time Period\"]//ancestor::div/mat-icon[@role=\"img\"][@data-mat-icon-name=\"fa-recycle\"]")).size());

        selectProfileBIEToReuseDialog =editBIEPage.reuseBIEOnNode("/Cancel Acknowledge Employee Work Time/Data Area/Cancel Acknowledge/Response Criteria");
        selectProfileBIEToReuseDialog.selectBIEToReuse(BIEResponseCriteria);

        editBIEPage.getNodeByPath("/Cancel Acknowledge Employee Work Time/Data Area/Cancel Acknowledge/Response Criteria");
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"Response Criteria\"]//ancestor::div/mat-icon[@role=\"img\"][@data-mat-icon-name=\"fa-recycle\"]")).size());

        selectProfileBIEToReuseDialog =editBIEPage.reuseBIEOnNode("/Cancel Acknowledge Employee Work Time/Data Area/Employee Work Time/Common Time Reporting");
        selectProfileBIEToReuseDialog.selectBIEToReuse(BIECommonTimeReporting);

        editBIEPage.getNodeByPath("/Cancel Acknowledge Employee Work Time/Data Area/Employee Work Time/Common Time Reporting");
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"Common Time Reporting\"]//ancestor::div/mat-icon[@role=\"img\"][@data-mat-icon-name=\"fa-recycle\"]")).size());

        selectProfileBIEToReuseDialog =editBIEPage.reuseBIEOnNode("/Cancel Acknowledge Employee Work Time/Data Area/Employee Work Time/Common Time Reporting/Contract Reference/Line Identifier Set");
        selectProfileBIEToReuseDialog.selectBIEToReuse(BIELineIdentifierSet);

        editBIEPage.getNodeByPath("/Cancel Acknowledge Employee Work Time/Data Area/Employee Work Time/Common Time Reporting/Contract Reference/Line Identifier Set");
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"Line Identifier Set\"]//ancestor::div/mat-icon[@role=\"img\"][@data-mat-icon-name=\"fa-recycle\"]")).size());

        selectProfileBIEToReuseDialog =editBIEPage.reuseBIEOnNode("/Cancel Acknowledge Employee Work Time/Data Area/Employee Work Time/Common Time Reporting/Work Location/Enterprise Unit");
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"Enterprise Unit\")]//ancestor::tr[1]/td[1]/mat-checkbox/label/span[1][1]")));
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"Line Identifier Set\")]//ancestor::tr[1]/td[1]/mat-checkbox/label/span[1][1]")));
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
