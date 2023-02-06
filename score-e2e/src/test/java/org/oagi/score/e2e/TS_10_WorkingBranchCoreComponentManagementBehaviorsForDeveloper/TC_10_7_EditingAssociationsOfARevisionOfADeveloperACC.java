package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.CoreComponentMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_7_EditingAssociationsOfARevisionOfADeveloperACC extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_10_7_TA_4")
    public void test_TA_4() {
        AppUserObject developer;
        NamespaceObject namespace;
        ReleaseObject release;
        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Candidate");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Candidate");
            coreComponentAPI.appendBCC(acc, bccp, "Candidate");

            ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Candidate");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Candidate");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Candidate");
            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Candidate");
            coreComponentAPI.appendASCC(acc, asccp, "Candidate");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
        accViewEditPage.backToWIP();

        accViewEditPage.expandTree(acc.getDen());
        accViewEditPage.goToNode(bccp.getPropertyTerm());
        accViewEditPage.setCardinalityMax(5);
        accViewEditPage.hitUpdateButton();
        accViewEditPage.moveToDraft();
        accViewEditPage.moveToCandidate();
        accViewEditPage.backToWIP();
        accViewEditPage.setCardinalityMax(-1);
        accViewEditPage.hitUpdateButton();
        accViewEditPage.moveToDraft();
        accViewEditPage.moveToCandidate();
        accViewEditPage.backToWIP();
        accViewEditPage.setCardinalityMax(6);
        accViewEditPage.hitUpdateButton();
        accViewEditPage.moveToDraft();
        accViewEditPage.moveToCandidate();
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
