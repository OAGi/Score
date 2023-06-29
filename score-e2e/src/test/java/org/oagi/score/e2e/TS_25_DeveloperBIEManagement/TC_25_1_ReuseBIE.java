package org.oagi.score.e2e.TS_25_DeveloperBIEManagement;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void test_TA_25_1_2_a() {

    }


    @Test
    public void test_TA_25_1_2_b() {

    }

    @Test
    public void test_TA_25_1_3() {

    }

    @Test
    public void test_TA_25_1_4() {

    }

    @Test
    public void test_TA_25_1_5() {

    }

    @Test
    public void test_TA_25_1_6() {

    }

    @Test
    public void test_TA_25_1_7() {

    }

    @Test
    public void test_TA_25_1_8() {

    }

    @Test
    public void test_TA_25_1_9() {

    }

    @Test
    public void test_TA_25_1_10() {

    }

    @Test
    public void test_TA_25_1_11() {

    }

    @Test
    public void test_TA_25_1_12() {

    }

    @Test
    public void test_TA_25_1_13() {

    }

    @Test
    public void test_TA_25_1_14() {

    }

    @Test
    public void test_TA_25_1_15() {

    }

    @Test
    public void test_TA_25_1_16() {

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
