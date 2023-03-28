package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.acc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_8_DeveloperACCStateManagement extends BaseTest {

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
    public void test_TA_10_8_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            coreComponentAPI.updateACC(acc);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("1", getText(accPanel.getRevisionField()));
        assertEquals("WIP", getText(accPanel.getStateField()));
        accViewEditPage.moveToDraft();
        accViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("Draft", getText(accPanel.getStateField()));
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPPanel asccpPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals("Draft", getText(asccpPanel.getStateField()));
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPPanel bccpPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPPanel();
        assertEquals("Draft", getText(bccpPanel.getStateField()));
    }

    @Test
    public void test_TA_10_8_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Draft");
            coreComponentAPI.updateACC(acc);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Draft");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Draft");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Draft");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Draft");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Draft");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Draft");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Draft");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("1", getText(accPanel.getRevisionField()));
        assertEquals("Draft", getText(accPanel.getStateField()));
        accViewEditPage.moveToCandidate();
        accViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("Candidate", getText(accPanel.getStateField()));
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPPanel asccpPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals("Candidate", getText(asccpPanel.getStateField()));
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPPanel bccpPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPPanel();
        assertEquals("Candidate", getText(bccpPanel.getStateField()));

    }

    @Test
    public void test_TA_10_8_3() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Candidate");
            coreComponentAPI.updateACC(acc);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Candidate");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Candidate");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Candidate");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Candidate");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Candidate");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Candidate");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Candidate");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("1", getText(accPanel.getRevisionField()));
        assertEquals("Candidate", getText(accPanel.getStateField()));
        accViewEditPage.backToWIP();
        accViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("WIP", getText(accPanel.getStateField()));
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPPanel asccpPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals("WIP", getText(asccpPanel.getStateField()));
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPPanel bccpPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPPanel();
        assertEquals("WIP", getText(bccpPanel.getStateField()));
    }

    @Test
    public void test_TA_10_8_4() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Draft");
            coreComponentAPI.updateACC(acc);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Draft");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Draft");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Draft");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Draft");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Draft");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Draft");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Draft");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("1", getText(accPanel.getRevisionField()));
        assertEquals("Draft", getText(accPanel.getStateField()));
        accViewEditPage.backToWIP();
        accViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("WIP", getText(accPanel.getStateField()));
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPPanel asccpPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals("WIP", getText(asccpPanel.getStateField()));
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPPanel bccpPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPPanel();
        assertEquals("WIP", getText(bccpPanel.getStateField()));
    }

}
