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
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
    public void test_TA_24_1_1_a() {
        ASCCPObject asccp, asccp_owner_usera;
        BCCPObject bccp;
        ACCObject acc;
        AppUserObject usera;
        AppUserObject userb;
        BusinessContextObject context;
        String prev_release = "10.8.5";
        ReleaseObject prevReleaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(prev_release);
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(usera, prevReleaseObject, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", prevReleaseObject.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            ACCObject acc_association = coreComponentAPI.createRandomACC(usera, prevReleaseObject, namespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc_association, usera, namespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);

            asccp_owner_usera = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
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




    }

    @Test
    public void test_TA_24_1_1_b() {

    }

    @Test
    public void test_TA_24_1_1_c() {

    }

    @Test
    public void test_TA_24_1_1_d() {

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
