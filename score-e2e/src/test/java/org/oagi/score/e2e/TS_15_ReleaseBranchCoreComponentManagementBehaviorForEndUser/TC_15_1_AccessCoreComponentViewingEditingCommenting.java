package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.CoreComponentMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_15_1_AccessCoreComponentViewingEditingCommenting extends BaseTest {
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

    private class RandomCoreComponentWithStateContainer {
        private AppUserObject appUser;
        private List<String> states = new ArrayList<>();
        private HashMap<String, ACCObject> stateACCs = new HashMap<>();
        private HashMap<String, ASCCPObject> stateASCCPs = new HashMap<>();
        private HashMap<String, BCCPObject> stateBCCPs = new HashMap<>();

        public RandomCoreComponentWithStateContainer(AppUserObject appUser, ReleaseObject release, NamespaceObject namespace, List<String> states) {
            this.appUser = appUser;
            this.states = states;


            for (int i = 0; i < this.states.size(); ++i) {
                ASCCPObject asccp;
                BCCPObject bccp;
                ACCObject acc;
                String state = this.states.get(i);

                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

                    acc = coreComponentAPI.createRandomACC(this.appUser, release, namespace, state);
                    DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
                    bccp = coreComponentAPI.createRandomBCCP(dataType, this.appUser, namespace, state);
                    BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, state);
                    bcc.setCardinalityMax(1);
                    coreComponentAPI.updateBCC(bcc);

                    ACCObject acc_association = coreComponentAPI.createRandomACC(this.appUser, release, namespace, state);
                    BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, this.appUser, namespace, state);
                    coreComponentAPI.appendBCC(acc_association, bccp_to_append, state);

                    asccp = coreComponentAPI.createRandomASCCP(acc_association, this.appUser, namespace, state);
                    ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, state);
                    ascc.setCardinalityMax(1);
                    coreComponentAPI.updateASCC(ascc);
                    stateACCs.put(state, acc);
                    stateASCCPs.put(state, asccp);
                    stateBCCPs.put(state, bccp);
                }
            }
        }

    }

    @Test
    public void test_TA_15_1_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        NamespaceObject namespace_endUser = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(endUser, release, namespace_endUser, ccStates);

        AppUserObject second_user = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(second_user);

        HomePage homePage = loginPage().signIn(second_user.getLoginId(), second_user.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());

            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(asccp.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());

            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(bccp.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());
        }
    }

    @Test
    public void test_TA_15_1_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, endUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, endUser, namespace, "WIP");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "WIP");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        /**
         * developer can edit the details of a CC that is in WIP state and owned by him
         */

        assertEquals("WIP", getText(accViewEditPage.getStateField()));
        assertDisabled(accViewEditPage.getStateField());
        assertDisabled(accViewEditPage.getGUIDField());
        assertDisabled(accViewEditPage.getDENField());
        assertEnabled(accViewEditPage.getObjectClassTermField());
        assertEnabled(accViewEditPage.getDefinitionField());
        assertEnabled(accViewEditPage.getDefinitionSourceField());
        assertEnabled(accViewEditPage.getNamespaceField());
        assertEnabled(accPanel.getComponentTypeSelectField());

        accViewEditPage.openPage(); // refresh the page to erase the snackbar message
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
        assertEquals("WIP", getText(bccPanelContainer.getBCCPanel().getStateField()));
        assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
        assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
        assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
        assertEnabled(bccPanelContainer.getBCCPanel().getDefinitionField());
        assertEnabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());

        accViewEditPage.openPage();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanelContainer asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
        asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);

        assertEquals("WIP", getText(asccPanelContainer.getASCCPanel().getStateField()));
        assertDisabled(asccPanelContainer.getASCCPanel().getStateField());
        assertDisabled(asccPanelContainer.getASCCPanel().getGUIDField());
        assertDisabled(asccPanelContainer.getASCCPanel().getDENField());
        assertEnabled(asccPanelContainer.getASCCPanel().getDefinitionField());
        assertEnabled(asccPanelContainer.getASCCPanel().getDefinitionSourceField());
    }

    @Test
    public void test_TA_15_1_3_and_TA_15_1_4() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        List<String> ccStates = new ArrayList<>();
        ccStates.add("QA");
        ccStates.add("Production");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(endUser, release, namespace, ccStates);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            viewEditCoreComponentPage.openPage();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());

            assertEquals(state, getText(accViewEditPage.getStateField()));
            assertDisabled(accViewEditPage.getStateField());
            assertDisabled(accViewEditPage.getGUIDField());
            assertDisabled(accViewEditPage.getDENField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getNamespaceField());
            assertDisabled(accViewEditPage.getCoreComponentTypeField());

            viewEditCoreComponentPage.openPage();   // refresh the page to erase the snackbar message
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPanelContainer asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
            asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);

            assertEquals(state, getText(asccPanelContainer.getASCCPanel().getStateField()));
            assertDisabled(asccPanelContainer.getASCCPanel().getStateField());
            assertDisabled(asccPanelContainer.getASCCPanel().getGUIDField());
            assertDisabled(asccPanelContainer.getASCCPanel().getDENField());
            assertDisabled(asccPanelContainer.getASCCPPanel().getPropertyTermField());
            assertDisabled(asccPanelContainer.getASCCPPanel().getDefinitionField());
            assertDisabled(asccPanelContainer.getASCCPPanel().getDefinitionSourceField());
            assertDisabled(asccPanelContainer.getASCCPPanel().getNamespaceSelectField());
        }

    }

    @Test
    public void test_TA_15_1_5_and_TA_15_1_6_and_TA_15_1_7() {

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(endUser, release, namespace, ccStates);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        HomePage homePage = loginPage().signIn(anotherUser.getLoginId(), anotherUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            viewEditCoreComponentPage.openPage();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            /**
             * developer can view but CANNOT edit the details of a CC that is in WIP state and owned by another developer
             * However, he can add comments.
             */

            assertEquals(state, getText(accViewEditPage.getStateField()));
            assertDisabled(accViewEditPage.getStateField());
            assertDisabled(accViewEditPage.getGUIDField());
            assertDisabled(accViewEditPage.getDENField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getNamespaceField());
            assertDisabled(accViewEditPage.getCoreComponentTypeField());

            viewEditCoreComponentPage.openPage();   // refresh the page to erase the snackbar message
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPanelContainer asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
            asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);

            assertEquals(state, getText(asccPanelContainer.getASCCPanel().getStateField()));
            assertDisabled(asccPanelContainer.getASCCPanel().getStateField());
            assertDisabled(asccPanelContainer.getASCCPanel().getGUIDField());
            assertDisabled(asccPanelContainer.getASCCPanel().getDENField());
            assertDisabled(asccPanelContainer.getASCCPPanel().getPropertyTermField());
            assertDisabled(asccPanelContainer.getASCCPPanel().getDefinitionField());
            assertDisabled(asccPanelContainer.getASCCPPanel().getDefinitionSourceField());
            assertDisabled(asccPanelContainer.getASCCPPanel().getNamespaceSelectField());
        }
    }

    @Test
    public void test_TA_15_1_7_amend_and_take_over_the_ownership() {

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Production");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(anotherUser, release, namespace, ccStates);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            viewEditCoreComponentPage.openPage();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
            ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
            assertEquals(state, getText(accPanel.getStateField()));
            assertDisabled(accPanel.getStateField());
            assertDisabled(accPanel.getGUIDField());
            assertDisabled(accPanel.getDENField());
            assertDisabled(accPanel.getObjectClassTermField());
            assertDisabled(accPanel.getDefinitionField());
            assertDisabled(accPanel.getDefinitionSourceField());

            accViewEditPage.hitAmendButton();
            accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
            accPanel = accViewEditPage.getACCPanel(accNode);
            assertEquals("WIP", getText(accPanel.getStateField()));
            assertEquals(endUser.getLoginId(), getText(accPanel.getOwnerField()));

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals("WIP", getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertEquals(endUser.getLoginId(), getText(bccPanelContainer.getBCCPanel().getOwnerField()));

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPanelContainer asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
            asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);

            assertEquals("WIP", getText(asccPanelContainer.getASCCPanel().getStateField()));
            assertEquals(endUser.getLoginId(), getText(asccPanelContainer.getASCCPanel().getOwnerField()));

        }
    }

    @Test
    public void test_TA_15_1_8() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        NamespaceObject namespace_endUser = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Published");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Published");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());

        assertEquals("Published", getText(accViewEditPage.getStateField()));
        assertDisabled(accViewEditPage.getStateField());
        assertDisabled(accViewEditPage.getGUIDField());
        assertDisabled(accViewEditPage.getDENField());
        assertDisabled(accViewEditPage.getObjectClassTermField());
        assertDisabled(accViewEditPage.getDefinitionField());
        assertDisabled(accViewEditPage.getDefinitionSourceField());
        assertDisabled(accViewEditPage.getNamespaceField());
        assertDisabled(accViewEditPage.getCoreComponentTypeField());

        accViewEditPage.openPage(); // refresh the page to erase the snackbar message
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
        assertEquals("Published", getText(bccPanelContainer.getBCCPanel().getStateField()));
        assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
        assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
        assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());

        accViewEditPage.openPage();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanelContainer asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
        asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);

        assertEquals("Published", getText(asccPanelContainer.getASCCPanel().getStateField()));
        assertDisabled(asccPanelContainer.getASCCPanel().getStateField());
        assertDisabled(asccPanelContainer.getASCCPanel().getGUIDField());
        assertDisabled(asccPanelContainer.getASCCPanel().getDENField());
        assertDisabled(asccPanelContainer.getASCCPPanel().getPropertyTermField());
        assertDisabled(asccPanelContainer.getASCCPPanel().getDefinitionField());
        assertDisabled(asccPanelContainer.getASCCPPanel().getDefinitionSourceField());
        assertDisabled(asccPanelContainer.getASCCPPanel().getNamespaceSelectField());

    }

    @Test
    public void test_TA_15_1_9() {

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.7.1");
        NamespaceObject namespace_endUser = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace_endUser, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, endUser, namespace_endUser, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace_endUser, "WIP");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, endUser, namespace_endUser, "WIP");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace_endUser, "WIP");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        WebElement tr = viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
        WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));
        tr = viewEditCoreComponentPage.getTableRecordByValue(bccp.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));
        tr = viewEditCoreComponentPage.getTableRecordByValue(asccp.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));

        viewEditCoreComponentPage.hitMoveToQAButton();

        viewEditCoreComponentPage.setState("QA");
        escape(getDriver());
        viewEditCoreComponentPage.hitSearchButton();

        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(acc.getDen(), endUser.getLoginId()).isDisplayed());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(asccp.getDen(), endUser.getLoginId()).isDisplayed());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(bccp.getDen(), endUser.getLoginId()).isDisplayed());

        tr = viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));
        tr = viewEditCoreComponentPage.getTableRecordByValue(bccp.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));
        tr = viewEditCoreComponentPage.getTableRecordByValue(asccp.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));

        viewEditCoreComponentPage.hitBackToWIPButton();
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        viewEditCoreComponentPage.setState("WIP");
        escape(getDriver());
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(acc.getDen(), endUser.getLoginId()).isDisplayed());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(asccp.getDen(), endUser.getLoginId()).isDisplayed());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(bccp.getDen(), endUser.getLoginId()).isDisplayed());

        tr = viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));
        tr = viewEditCoreComponentPage.getTableRecordByValue(bccp.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));
        tr = viewEditCoreComponentPage.getTableRecordByValue(asccp.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));

        viewEditCoreComponentPage.hitMoveToQAButton();
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        viewEditCoreComponentPage.setState("QA");
        escape(getDriver());
        viewEditCoreComponentPage.hitSearchButton();

        tr = viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));
        tr = viewEditCoreComponentPage.getTableRecordByValue(bccp.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));
        tr = viewEditCoreComponentPage.getTableRecordByValue(asccp.getDen());
        td = viewEditCoreComponentPage.getColumnByName(tr, "select");
        assertEnabled(td.findElement(By.tagName("mat-checkbox")));
        click(td.findElement(By.tagName("mat-checkbox")));

        viewEditCoreComponentPage.hitMoveToProductionButton();
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        viewEditCoreComponentPage.setState("Production");
        escape(getDriver());
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(acc.getDen(), endUser.getLoginId()).isDisplayed());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(asccp.getDen(), endUser.getLoginId()).isDisplayed());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(bccp.getDen(), endUser.getLoginId()).isDisplayed());

    }
}
