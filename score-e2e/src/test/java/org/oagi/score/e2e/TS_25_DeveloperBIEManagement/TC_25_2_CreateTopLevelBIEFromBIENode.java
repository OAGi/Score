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
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_25_2_CreateTopLevelBIEFromBIENode extends BaseTest {
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
    public void test_TA_25_2_1_a_b_c() {
        ASCCPObject asccp, asccp_for_devx, asccp_lv2;
        ACCObject acc, acc_association, acc_association_lv2;
        AppUserObject devx, devy;
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
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

            NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx, library);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            /**
             * The owner of the ASCCP is usera
             */
            acc = coreComponentAPI.createRandomACC(devx, currentReleaseObject, developerNamespace, "Published");
            acc_association = coreComponentAPI.createRandomACC(devx, currentReleaseObject, developerNamespace, "Published");
            acc_association_lv2 = coreComponentAPI.createRandomACC(devx, currentReleaseObject, developerNamespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, devx, developerNamespace, "Published");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_association_lv2, asccp, "Published");
            asccp_lv2 = coreComponentAPI.createRandomASCCP(acc_association_lv2, devx, developerNamespace, "Published");
            ASCCObject ascc_lv2 = coreComponentAPI.appendASCC(acc_association, asccp_lv2, "Published");
            asccp_for_devx = coreComponentAPI.createRandomASCCP(acc_association, devx, developerNamespace, "Published");
            devxBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, devx, "WIP");
            devyBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp_for_devx, devy, "WIP");

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
        topLevelASBIEPPanel.setRemark("devxBIE remark");
        topLevelASBIEPPanel.setContextDefinition("devxBIE definition");
        topLevelASBIEPPanel.setBusinessTerm("devxBIE business term");
        topLevelASBIEPPanel.setStatus("devxBIE status");
        editBIEPage.hitUpdateButton();

        homePage.logout();
        homePage = loginPage().signIn(devy.getLoginId(), devy.getPassword());
        bieMenu = homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(current_release);
        viewEditBIEPage.setDEN(devyBIE.getDen());
        viewEditBIEPage.hitSearchButton();

        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        selectProfileBIEToReuseDialog.selectBIEToReuse(devxBIE);
        editBIEPage.getNodeByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        WebElement asccpNode = editBIEPage.getNodeByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        EditBIEPage.ReusedASBIEPanel reusedASBIEPanel = editBIEPage.getReusedASBIEPanel(asccpNode);

        asccpNode = editBIEPage.getNodeByPath("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        asbiePanel.setCardinalityMax(13);
        asbiePanel.setCardinalityMin(7);
        asbiePanel.setContextDefinition("association of the Reused BIE");
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        editBIEPage.MakeBIEReusableOnNode("/" + asccp_for_devx.getPropertyTerm() + "/" + asccp_lv2.getPropertyTerm());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setDEN(asccp_lv2.getDen());
        viewEditBIEPage.hitSearchButton();
        tr = viewEditBIEPage.getTableRecordAtIndex(1);
        editBIEPage = viewEditBIEPage.openEditBIEPage(tr);

        asccpNode = editBIEPage.getNodeByPath("/" + asccp_lv2.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"" + asccp.getPropertyTerm() + "\"]//ancestor::div[1]/fa-icon")).size());

        asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        assertEquals("13", getText(asbiePanel.getCardinalityMaxField()));
        assertEquals("7", getText(asbiePanel.getCardinalityMinField()));
        editBIEPage.openPage();
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals(devy.getLoginId(), getText(topLevelASBIEPPanel.getOwnerField()));
    }
}
