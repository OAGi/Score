package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper;

import org.apache.commons.lang3.RandomUtils;
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
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_1_Core_Component_Access extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void pressEscape(){
        invisibilityOfLoadingContainerElement(getDriver());
        Actions action = new Actions(getDriver());
        action.sendKeys(Keys.ESCAPE).build().perform();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }
    private class RandomCoreComponentWithStateContainer {
        private AppUserObject appUser;
        private List<String> states = new ArrayList<>();
        private HashMap<String, ACCObject> stateACCs= new HashMap<>();
        private HashMap<String, ASCCPObject> stateASCCPs = new HashMap<>();
        private HashMap<String, BCCPObject> stateBCCPs = new HashMap<>();
        public RandomCoreComponentWithStateContainer(AppUserObject appUser, ReleaseObject release, NamespaceObject namespace, List<String> states)
                                            {
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
                    stateBCCPs.put(state,bccp);
                }
            }
        }

    }

    @Test
    @DisplayName("TC_10_1_TA_1")
    public void test_TA_1(){
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        AppUserObject developer2 = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer2);

        HomePage homePage = loginPage().signIn(developer2.getLoginId(), developer2.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry: randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
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
    @DisplayName("TC_10_1_TA_2")
    public void test_TA_2(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
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
    @DisplayName("TC_10_1_TA_3")
    public void test_TA_3(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        AppUserObject developer2 = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer2);
        HomePage homePage = loginPage().signIn(developer2.getLoginId(), developer2.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
        /**
         * developer can view but CANNOT edit the details of a CC that is in WIP state and owned by another developer
         * However, he can add comments.
         */

        assertEquals("WIP", getText(accViewEditPage.getStateField()));
        assertDisabled(accViewEditPage.getStateField());
        assertDisabled(accViewEditPage.getGUIDField());
        assertDisabled(accViewEditPage.getDENField());
        assertDisabled(accViewEditPage.getObjectClassTermField());
        assertDisabled(accViewEditPage.getDefinitionField());
        assertDisabled(accViewEditPage.getDefinitionSourceField());
        assertDisabled(accViewEditPage.getNamespaceField());
        assertDisabled(accViewEditPage.getCoreComponentTypeField());
        accViewEditPage.openCommentsDialog("/" + acc.getDen());

        accViewEditPage.openPage(); // refresh the page to erase the snackbar message
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
        assertEquals("WIP", getText(bccPanelContainer.getBCCPanel().getStateField()));
        assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
        assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
        assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
        accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + bccp.getPropertyTerm());

        accViewEditPage.openPage();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanelContainer asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
        asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);

        assertEquals("WIP", getText(asccPanelContainer.getASCCPanel().getStateField()));
        assertDisabled(asccPanelContainer.getASCCPanel().getStateField());
        assertDisabled(asccPanelContainer.getASCCPanel().getGUIDField());
        assertDisabled(asccPanelContainer.getASCCPanel().getDENField());
        assertDisabled(asccPanelContainer.getASCCPPanel().getPropertyTermField());
        assertDisabled(asccPanelContainer.getASCCPPanel().getDefinitionField());
        assertDisabled(asccPanelContainer.getASCCPPanel().getDefinitionSourceField());
        assertDisabled(asccPanelContainer.getASCCPPanel().getNamespaceSelectField());
        accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
    }

    @Test
    @DisplayName("TC_10_1_TA_4")
    public void test_TA_4(){
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Draft");
        ccStates.add("Candidate");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        AppUserObject developer2 = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer2);

        HomePage homePage = loginPage().signIn(developer2.getLoginId(), developer2.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry: randomCoreComponentWithStateContainer.stateACCs.entrySet()){
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
            accViewEditPage.openCommentsDialog("/" + acc.getDen());

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
            accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + bccp.getPropertyTerm());

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
            accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        }

    }

    @Test
    @DisplayName("TC_10_1_TA_5")
    public void test_TA_5(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Published");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        AppUserObject developer2 = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer2);

        HomePage homePage = loginPage().signIn(developer2.getLoginId(), developer2.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry: randomCoreComponentWithStateContainer.stateACCs.entrySet()){
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
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
            accViewEditPage.openCommentsDialog("/" + acc.getDen());

            accViewEditPage.openPage(); // refresh the page to erase the snackbar message
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
            accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + bccp.getPropertyTerm());

            accViewEditPage.openPage();
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
            accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        }

    }

    @Test
    @DisplayName("TC_10_1_TA_6")
    public void test_TA_6(){

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch("Working");
        By ADD_CC_ICON_LOCATOR =
                By.xpath("//mat-icon[contains(text(), \"add\")]");
        assertEquals(0, getDriver().findElements(ADD_CC_ICON_LOCATOR).size());

    }

    @Test
    @DisplayName("TC_10_1_TA_7")
    public void test_TA_7(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Deleted");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        AppUserObject developer2 = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer2);

        HomePage homePage = loginPage().signIn(developer2.getLoginId(), developer2.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry: randomCoreComponentWithStateContainer.stateACCs.entrySet()){
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
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
            accViewEditPage.openCommentsDialog("/" + acc.getDen());

            accViewEditPage.openPage(); // refresh the page to erase the snackbar message
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
            accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + bccp.getPropertyTerm());

            accViewEditPage.openPage();
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
            accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        }
    }

    @Test
    @DisplayName("TC_10_1_TA_8")
    public void test_TA_8(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Deleted");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry: randomCoreComponentWithStateContainer.stateACCs.entrySet()){
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
            /**
             * developer cannot edit details of a deleted CC owned by him. He can add comments
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
            accViewEditPage.openCommentsDialog("/" + acc.getDen());

            accViewEditPage.openPage(); // refresh the page to erase the snackbar message
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
            accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + bccp.getPropertyTerm());

            accViewEditPage.openPage();
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
            accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        }
    }

    @Test
    @DisplayName("TC_10_1_TA_9")
    public void test_TA_9(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Deleted");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        AppUserObject developer2 = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer2);

        HomePage homePage = loginPage().signIn(developer2.getLoginId(), developer2.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry: randomCoreComponentWithStateContainer.stateACCs.entrySet()){
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
            /**
             * developer can cannot edit details of a deleted CC owned by another developer. He can add comments.
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
            accViewEditPage.openCommentsDialog("/" + acc.getDen());

            accViewEditPage.openPage(); // refresh the page to erase the snackbar message
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
            accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + bccp.getPropertyTerm());

            accViewEditPage.openPage();
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
            accViewEditPage.openCommentsDialog("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        }

    }

    @Test
    @DisplayName("TC_10_1_TA_10")
    public void test_TA_10(){


    }
    @Test
    @DisplayName("TC_10_1_TA_11")
    public void test_TA_11(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        AppUserObject developer2 = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer2);

        HomePage homePage = loginPage().signIn(developer2.getLoginId(), developer2.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry: randomCoreComponentWithStateContainer.stateACCs.entrySet()){
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            /**
             * developer can filter Core Components based on their Type.
             */
            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(3000L));
            click(viewEditCoreComponentPage.getTypeSelectField());
            List<WebElement> options = getDriver().findElements(By.cssSelector("mat-option"));
            for (String ccState : Arrays.asList("ASCCP", "BCCP", "CDT", "BDT" )){
                List<WebElement> result = options.stream().filter(e -> ccState.equals(getText(e))).collect(Collectors.toList());
                result.get(0).click();
            }
            escape(getDriver());
            viewEditCoreComponentPage.hitSearchButton();
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(acc.getDen(), developer.getLoginId()).isDisplayed());

            // search by "ASCCP" type
            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(3000L));
            click(viewEditCoreComponentPage.getTypeSelectField());
            options = getDriver().findElements(By.cssSelector("mat-option"));
            for (String ccState : Arrays.asList("ACC", "BCCP", "CDT", "BDT" )){
                List<WebElement> result = options.stream().filter(e -> ccState.equals(getText(e))).collect(Collectors.toList());
                result.get(0).click();
            }
            escape(getDriver());
            viewEditCoreComponentPage.hitSearchButton();
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(asccp.getDen(), developer.getLoginId()).isDisplayed());

            // search by "BCCP" type
            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(3000L));
            click(viewEditCoreComponentPage.getTypeSelectField());
            options = getDriver().findElements(By.cssSelector("mat-option"));
            for (String ccState : Arrays.asList("ACC","ASCCP", "CDT", "BDT" )){
                List<WebElement> result = options.stream().filter(e -> ccState.equals(getText(e))).collect(Collectors.toList());
                result.get(0).click();
            }
            escape(getDriver());
            viewEditCoreComponentPage.hitSearchButton();
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(bccp.getDen(), developer.getLoginId()).isDisplayed());
        }

    }
    @Test
    @DisplayName("TC_10_1_TA_12")
    public void test_TA_12(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        AppUserObject developer2 = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer2);

        HomePage homePage = loginPage().signIn(developer2.getLoginId(), developer2.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry: randomCoreComponentWithStateContainer.stateACCs.entrySet()){
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            /**
             * developer can filter Core Components based on their Type.
             */
            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(3000L));
            click(viewEditCoreComponentPage.getStateSelectField());
            List<WebElement> options = getDriver().findElements(By.cssSelector("mat-option"));

            // search by state
            List<WebElement> stateOption = options.stream().filter(e -> state.equals(getText(e))).collect(Collectors.toList());
            stateOption.get(0).click();
            escape(getDriver());
            viewEditCoreComponentPage.hitSearchButton();
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(acc.getDen(), developer.getLoginId()).isDisplayed());
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(asccp.getDen(), developer.getLoginId()).isDisplayed());
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(bccp.getDen(), developer.getLoginId()).isDisplayed());

        }

    }
    @Test
    @DisplayName("TC_10_1_TA_13")
    public void test_TA_13(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry: randomCoreComponentWithStateContainer.stateACCs.entrySet()){
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            /**
             * developer can filter Core Components based on their Updated Date.
             */
            LocalDateTime startTime = LocalDateTime.of(
                    2013,
                    RandomUtils.nextInt(1, 13),
                    RandomUtils.nextInt(1, 29),
                    RandomUtils.nextInt(0, 24),
                    RandomUtils.nextInt(0, 60),
                    RandomUtils.nextInt(0, 60)
            );
            LocalDateTime endTime = LocalDateTime.of(
                    2015,
                    RandomUtils.nextInt(1, 13),
                    RandomUtils.nextInt(1, 29),
                    RandomUtils.nextInt(0, 24),
                    RandomUtils.nextInt(0, 60),
                    RandomUtils.nextInt(0, 60)
            );

            LocalDateTime creationTime = LocalDateTime.of(
                    2014,
                    RandomUtils.nextInt(1, 13),
                    RandomUtils.nextInt(1, 29),
                    RandomUtils.nextInt(0, 24),
                    RandomUtils.nextInt(0, 60),
                    RandomUtils.nextInt(0, 60)
            );
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc.setCreationTimestamp(creationTime);
            acc.setLastUpdateTimestamp(creationTime);
            coreComponentAPI.updateACC(acc);
            asccp.setCreationTimestamp(creationTime);
            asccp.setLastUpdateTimestamp(creationTime);
            coreComponentAPI.updateASCCP(asccp);
            bccp.setCreationTimestamp(creationTime);
            bccp.setLastUpdateTimestamp(creationTime);
            coreComponentAPI.updateBCCP(bccp);

            // search by Updated date
            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(3000L));
            viewEditCoreComponentPage.setUpdatedStartDate(startTime);
            viewEditCoreComponentPage.setUpdatedEndDate(endTime);
            viewEditCoreComponentPage.hitSearchButton();
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(acc.getDen(), developer.getLoginId()).isDisplayed());
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(asccp.getDen(), developer.getLoginId()).isDisplayed());
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(bccp.getDen(), developer.getLoginId()).isDisplayed());

        }
    }
    @Test
    @DisplayName("TC_10_1_TA_14")
    public void test_TA_14(){
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setDEN("\"Action Code\"");
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("Action Code. Code", "oagis").isDisplayed());
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"Corrective Action Type Code. Code\")]")).size());
    }

    @Test
    @DisplayName("TC_10_1_TA_15")
    public void test_TA_15(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setDefinition("Notice Document");
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("ASN Reference. Document Reference", "oagis").isDisplayed());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("Show Receive Delivery. Show Receive Delivery", "oagis").isDisplayed());

        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setDefinition("\"Notice Document\"");
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("ASN Reference. Document Reference", "oagis").isDisplayed());
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"Show Receive Delivery. Show Receive Delivery\")]")).size());
    }
    @Test
    @DisplayName("TC_10_1_TA_16")
    public void test_TA_16(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setModule("Model\\Platform\\2_6\\Common\\Components\\Components");
        viewEditCoreComponentPage.hitSearchButton();
        assertEquals(0, (getDriver().findElements(By.xpath("//*[contains(text(),\"Model\\OAGIS-Nouns\")]"))).size());

        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setModule("Master");
        viewEditCoreComponentPage.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-chip[.=\"BCC\"]")).size());
    }
    @Test
    @DisplayName("TC_10_1_TA_17")
    public void test_TA_17(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        waitFor(Duration.ofMillis(3000L));
        click(viewEditCoreComponentPage.getComponentTypeSelectField());
        List<WebElement> options = getDriver().findElements(By.cssSelector("mat-option"));
        // developer can search for Core Components based only on their Component Type
        List<WebElement> baseOption = options.stream().filter(e -> "Base (Abstract)".equals(getText(e))).collect(Collectors.toList());
        baseOption.get(0).click();
        escape(getDriver());
        viewEditCoreComponentPage.setDEN("\"Financial Account Reference\"");
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("Financial Account Reference Base. Details", "oagis").isDisplayed());
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"Financial Account Reference Identification. Details\")]")).size());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        click(viewEditCoreComponentPage.getComponentTypeSelectField());
        options = getDriver().findElements(By.cssSelector("mat-option"));
        // developer can search for Core Components based only on their Component Type
        for (String componentState : Arrays.asList( "Base (Abstract)", "Semantics")){
            List<WebElement> result = options.stream().filter(e -> componentState.equals(getText(e))).collect(Collectors.toList());
            result.get(0).click();
        }
        escape(getDriver());
        viewEditCoreComponentPage.setDEN("\"Financial Account Reference\"");
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("Financial Account Reference Identification. Details", "oagis").isDisplayed());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("Financial Account Reference Base. Details", "oagis").isDisplayed());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        click(viewEditCoreComponentPage.getComponentTypeSelectField());
        options = getDriver().findElements(By.cssSelector("mat-option"));
        // developer can search for Core Components based only on their Component Type
        for (String componentState : Arrays.asList( "Extension", "Semantics")){
            List<WebElement> result = options.stream().filter(e -> componentState.equals(getText(e))).collect(Collectors.toList());
            result.get(0).click();
        }
        escape(getDriver());
        viewEditCoreComponentPage.setDEN("\"Financial Account Reference\"");
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("Financial Account Reference Extension. Details", "oagis").isDisplayed());
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"Financial Account Reference Base. Details\")]")).size());


        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        click(viewEditCoreComponentPage.getComponentTypeSelectField());
        options = getDriver().findElements(By.cssSelector("mat-option"));
        // developer can search for Core Components based only on their Component Type
        for (String componentState : Arrays.asList( "Extension", "Semantics")){
            List<WebElement> result = options.stream().filter(e -> componentState.equals(getText(e))).collect(Collectors.toList());
            result.get(0).click();
        }
        escape(getDriver());
        viewEditCoreComponentPage.setDEN("\"Transaction\"");
        viewEditCoreComponentPage.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"Inventory Transaction Group. Details\")]")).size());
        assertTrue(1 <= getDriver().findElements(By.xpath("//*[contains(text(),\"Payment Transaction Extension. Details\")]")).size());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        click(viewEditCoreComponentPage.getComponentTypeSelectField());
        options = getDriver().findElements(By.cssSelector("mat-option"));
        // developer can search for Core Components based only on their Component Type
        for (String componentState : Arrays.asList( "OAGIS10 Nouns", "Semantic Group")){
            List<WebElement> result = options.stream().filter(e -> componentState.equals(getText(e))).collect(Collectors.toList());
            result.get(0).click();
        }
        escape(getDriver());
        viewEditCoreComponentPage.setDEN("\"Transaction\"");
        viewEditCoreComponentPage.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"Payment Transaction Extension. Details\")]")).size());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        viewEditCoreComponentPage.getComponentTypeSelectField().click();
        options = getDriver().findElements(By.cssSelector("mat-option"));
        // developer can search for Core Components based only on their Component Type
        for (String componentState : Arrays.asList( "OAGIS10 Nouns", "OAGIS10 BODs")){
            List<WebElement> result = options.stream().filter(e -> componentState.equals(getText(e))).collect(Collectors.toList());
            result.get(0).click();
        }
        escape(getDriver());
        viewEditCoreComponentPage.setDEN("\"Transaction\"");
        viewEditCoreComponentPage.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"Payment Transaction Extension. Details\")]")).size());

    }

    @Test
    @DisplayName("TC_10_1_TA_18")
    public void test_TA_18(){

    }

    @Test
    @DisplayName("TC_10_1_TA_19")
    public void test_TA_19(){
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        for (Map.Entry<String, ACCObject> entry: randomCoreComponentWithStateContainer.stateACCs.entrySet()){
            ACCObject acc;
            ASCCPObject asccp;
            BCCPObject bccp;
            String state = entry.getKey();
            acc = entry.getValue();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            /**
             * developer can move states of several CCs in one shot on the view/edit CC page
             */
            assertEquals(state, getText(accViewEditPage.getStateField()));
            assertDisabled(accViewEditPage.getStateField());
            //Transfer the ACC ownership
            if (state.equals("WIP")){
                assertEquals(developer.getLoginId(), getText(accViewEditPage.getOwnerField()));
                assertDisabled(accViewEditPage.getOwnerField());
                viewEditCoreComponentPage.openPage();
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                WebElement tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
                WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
                assertTrue(td.findElement(By.className("mat-icon")).isEnabled());
                TransferCCOwnershipDialog transferCCOwnershipDialog =
                        viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
                transferCCOwnershipDialog.transfer(anotherDeveloper.getLoginId());

                //verify the ownership is transferred
                accViewEditPage.openPage();
                assertEquals(anotherDeveloper.getLoginId(), getText(accViewEditPage.getOwnerField()));
                assertDisabled(accViewEditPage.getOwnerField());
                homePage.logout();
                homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());

                //transfer the ownership back
                viewEditCoreComponentPage.openPage();
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
                td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
                assertTrue(td.findElement(By.className("mat-icon")).isEnabled());
                transferCCOwnershipDialog =
                        viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
                transferCCOwnershipDialog.transfer(developer.getLoginId());
                accViewEditPage.openPage();
                assertEquals(developer.getLoginId(), getText(accViewEditPage.getOwnerField()));
                assertDisabled(accViewEditPage.getOwnerField());
                homePage.logout();
                homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
            }

            //ACC change state
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
            String accState = getText(accViewEditPage.getACCPanel(accNode).getStateField());
            if (accState.equals("WIP")){
                accViewEditPage.moveToDraft();
                assertEquals("Draft", getText(accViewEditPage.getACCPanel(accNode).getStateField()));
            } else if (accState.equals("Draft")){
                accViewEditPage.moveToCandidate();
                assertEquals("Candidate", getText(accViewEditPage.getACCPanel(accNode).getStateField()));
            } else if (accState.equals("Candidate")){
                accViewEditPage.backToWIP();
                assertEquals("WIP", getText(accViewEditPage.getACCPanel(accNode).getStateField()));
            }
            //BCCP panel
            accViewEditPage.openPage(); // refresh the page to erase the snackbar message
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());

            //Transfer the BCCP ownership
            if (state.equals("WIP")){
                assertEquals(developer.getLoginId(), getText(accViewEditPage.getOwnerField()));
                assertDisabled(accViewEditPage.getOwnerField());
                viewEditCoreComponentPage.openPage();
                viewEditCoreComponentPage.setDEN(bccp.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                WebElement tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
                WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
                assertTrue(td.findElement(By.className("mat-icon")).isEnabled());
                TransferCCOwnershipDialog transferCCOwnershipDialog =
                        viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
                transferCCOwnershipDialog.transfer(anotherDeveloper.getLoginId());
                //verify the ownership is transferred
                accViewEditPage.openPage();
                bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
                bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
                assertEquals(anotherDeveloper.getLoginId(), getText(bccPanelContainer.getBCCPPanel().getOwnerField()));
                assertDisabled(bccPanelContainer.getBCCPPanel().getOwnerField());
                homePage.logout();
                homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());

                //transfer the ownership back
                viewEditCoreComponentPage.openPage();
                viewEditCoreComponentPage.setDEN(bccp.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
                td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
                assertTrue(td.findElement(By.className("mat-icon")).isEnabled());
                transferCCOwnershipDialog =
                        viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
                transferCCOwnershipDialog.transfer(developer.getLoginId());
                accViewEditPage.openPage();
                bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
                bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
                assertEquals(developer.getLoginId(), getText(bccPanelContainer.getBCCPPanel().getOwnerField()));
                assertDisabled(bccPanelContainer.getBCCPPanel().getOwnerField());
                homePage.logout();
                homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
            }
            //BCCP state change
            accViewEditPage.openPage();
            bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            String bccState = getText(bccPanelContainer.getBCCPanel().getStateField());
            if (bccState.equals("WIP")){
                accViewEditPage.moveToDraft();
                assertEquals("Draft", getText(bccPanelContainer.getBCCPanel().getStateField()));
            } else if (bccState.equals("Draft")){
                accViewEditPage.moveToCandidate();
                assertEquals("Candidate", getText(bccPanelContainer.getBCCPanel().getStateField()));
            } else if (bccState.equals("Candidate")){
                accViewEditPage.backToWIP();
                assertEquals("WIP", getText(bccPanelContainer.getBCCPanel().getStateField()));
            }

            //ASCCP Panel
            accViewEditPage.openPage();
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPanelContainer asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
            assertEquals(state, getText(asccPanelContainer.getASCCPPanel().getStateField()));
            assertDisabled(asccPanelContainer.getASCCPPanel().getStateField());

            //Transfer the ASCCP ownership
            if (state.equals("WIP")){
                assertEquals(developer.getLoginId(), getText(accViewEditPage.getOwnerField()));
                assertDisabled(accViewEditPage.getOwnerField());
                viewEditCoreComponentPage.openPage();
                viewEditCoreComponentPage.setDEN(asccp.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                WebElement tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
                WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
                assertTrue(td.findElement(By.className("mat-icon")).isEnabled());
                TransferCCOwnershipDialog transferCCOwnershipDialog =
                        viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
                transferCCOwnershipDialog.transfer(anotherDeveloper.getLoginId());
                //verify the ownership is transferred
                accViewEditPage.openPage();
                asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
                asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
                assertEquals(anotherDeveloper.getLoginId(), getText(asccPanelContainer.getASCCPPanel().getOwnerField()));
                assertDisabled(asccPanelContainer.getASCCPPanel().getOwnerField());
                homePage.logout();
                homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());

                //transfer the ownership back
                viewEditCoreComponentPage.openPage();
                viewEditCoreComponentPage.setDEN(asccp.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
                td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
                assertTrue(td.findElement(By.className("mat-icon")).isEnabled());
                transferCCOwnershipDialog =
                        viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
                transferCCOwnershipDialog.transfer(developer.getLoginId());
                accViewEditPage.openPage();
                asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
                asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
                assertEquals(developer.getLoginId(), getText(asccPanelContainer.getASCCPPanel().getOwnerField()));
                assertDisabled(asccPanelContainer.getASCCPPanel().getOwnerField());
                homePage.logout();
                homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
            }

            //ASCCP state change
            accViewEditPage.openPage();
            asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
            String asccState = getText(asccPanelContainer.getASCCPanel().getStateField());
            if (asccState.equals("WIP")){
                accViewEditPage.moveToDraft();
                assertEquals("Draft", getText(asccPanelContainer.getASCCPanel().getStateField()));
            } else if (asccState.equals("Draft")){
                accViewEditPage.moveToCandidate();
                assertEquals("Candidate", getText(asccPanelContainer.getASCCPanel().getStateField()));
            } else if (asccState.equals("Candidate")){
                accViewEditPage.backToWIP();
                assertEquals("WIP", getText(asccPanelContainer.getASCCPanel().getStateField()));
            }
        }
    }

    @Test
    @DisplayName("TC_10_1_TA_20")
    public void test_TA_20(){

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Identifier Set. Details", release.getReleaseNumber());
        FindWhereUsedDialog findWhereUsedDialog = accViewEditPage.findWhereUsed("/" + "Identifier Set. Details");
        assertTrue(findWhereUsedDialog.getTableRecordByValue("Item Identifier Set").isDisplayed());
        assertTrue(findWhereUsedDialog.getTableRecordByValue("Tax Identifier Set").isDisplayed());
        assertTrue(findWhereUsedDialog.getTableRecordByValue("Party Identifier Set").isDisplayed());
        assertTrue(findWhereUsedDialog.getTableRecordByValue("Line Identifier Set").isDisplayed());
        assertTrue(findWhereUsedDialog.getTableRecordByValue("Document Identifier Set").isDisplayed());
        assertTrue(findWhereUsedDialog.getTableRecordByValue("Identifier Set").isDisplayed());
        assertTrue(findWhereUsedDialog.getTableRecordByValue("Item Identifier Set").isDisplayed());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Query Base. Details", release.getReleaseNumber());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + "Query Base. Details" + "/" + "Response Code");
        findWhereUsedDialog = accViewEditPage.findWhereUsed("/" + "Query Base. Details" + "/" + "Response Code");
        assertTrue(findWhereUsedDialog.getTableRecordByValue("Query Base").isDisplayed());
        assertTrue(findWhereUsedDialog.getTableRecordByValue("Authorization ABIE").isDisplayed());

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUser);
        homePage.logout();
        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");

        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(endUser, release, namespace, ccStates);

        ACCObject ACCendUserWIP, ACCendUserQA, ACCForBase;
        ASCCPObject ASCCPendUserQA;
        BCCPObject BCCPendUserQA;
        ACCendUserWIP = randomCoreComponentWithStateContainer.stateACCs.get("WIP");
        ACCendUserQA = randomCoreComponentWithStateContainer.stateACCs.get("QA");
        ASCCPendUserQA = randomCoreComponentWithStateContainer.stateASCCPs.get("QA");
        BCCPendUserQA = randomCoreComponentWithStateContainer.stateBCCPs.get("QA");
        ACCForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "QA");
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCendUserWIP.getAccManifestId());
        SelectAssociationDialog appendAssociationDialog = accViewEditPage.appendPropertyAtLast("/" + ACCendUserWIP.getDen());
        appendAssociationDialog.selectAssociation(ASCCPendUserQA.getDen());
        appendAssociationDialog = accViewEditPage.appendPropertyAtLast("/" + ACCendUserWIP.getDen());
        appendAssociationDialog.selectAssociation(BCCPendUserQA.getDen());

        ACCSetBaseACCDialog accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + ACCendUserWIP.getDen());
        accViewEditPage = accSetBaseACCDialog.hitApplyButton(ACCForBase.getDen());
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCendUserWIP.getAccManifestId());
        findWhereUsedDialog = accViewEditPage.findWhereUsed("/" + ACCendUserWIP.getDen() + "/" + ASCCPendUserQA.getPropertyTerm());
        assertTrue(findWhereUsedDialog.getTableRecordByValue(ACCendUserWIP.getDen()).isDisplayed());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCendUserWIP.getAccManifestId());
        findWhereUsedDialog = accViewEditPage.findWhereUsed("/" + ACCendUserWIP.getDen() + "/" + BCCPendUserQA.getPropertyTerm());
        assertTrue(findWhereUsedDialog.getTableRecordByValue(ACCendUserWIP.getDen()).isDisplayed());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCForBase.getAccManifestId());
        findWhereUsedDialog = accViewEditPage.findWhereUsed("/" + ACCForBase.getDen());
        assertTrue(findWhereUsedDialog.getTableRecordByValue(ACCendUserWIP.getDen()).isDisplayed());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCForBase.getAccManifestId());
        accViewEditPage.backToWIP();
        accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + ACCForBase.getDen());
        accSetBaseACCDialog.hitApplyButton("Customer Credit Base. Details");

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Customer Credit Base. Details", release.getReleaseNumber());
        accViewEditPage.findWhereUsed("/" + "Customer Credit Base. Details");
        assertTrue(findWhereUsedDialog.getTableRecordByValue(ACCForBase.getDen()).isDisplayed());
        assertTrue(findWhereUsedDialog.getTableRecordByValue("Customer Credit. Details").isDisplayed());
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
