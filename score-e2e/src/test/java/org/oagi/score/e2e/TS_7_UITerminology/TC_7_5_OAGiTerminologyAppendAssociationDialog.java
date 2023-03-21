package org.oagi.score.e2e.TS_7_UITerminology;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
public class TC_7_5_OAGiTerminologyAppendAssociationDialog extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private String release = "10.8.4";

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_7_5_TA_1_and_TA_2")
    public void test_TA_1_and_TA_2() {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ASCCPObject asccp;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        homePage.getLoginIDMenu().checkOAGISTerminology();

        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

        // TODO:
        // Can't open the context menu in a small size of the screen.
        getDriver().manage().window().maximize();
        SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");

        String moduleFieldLabel = selectCCPropertyPage.getModuleFieldLabel();
        assertEquals("Module (Part of schema file path, no extension)", moduleFieldLabel);
        String denFieldLabel = selectCCPropertyPage.getDENFieldLabel();
        assertEquals("DEN (Dictionary Entry Name)", denFieldLabel);
    }

    @Test
    @DisplayName("TC_7_5_TA_3_and_TA_4")
    public void test_TA_3_and_TA_4() {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ASCCPObject asccp;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        homePage.getLoginIDMenu().checkOAGISTerminology();

        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
        // TODO:
        // Can't open the context menu in a small size of the screen.
        getDriver().manage().window().maximize();
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
        String moduleFieldLabel = selectCCPropertyPage.getModuleFieldLabel();
        assertEquals("Module (Part of schema file path, no extension)", moduleFieldLabel);
        String denFieldLabel = selectCCPropertyPage.getDENFieldLabel();
        assertEquals("DEN (Dictionary Entry Name)", denFieldLabel);
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

}
