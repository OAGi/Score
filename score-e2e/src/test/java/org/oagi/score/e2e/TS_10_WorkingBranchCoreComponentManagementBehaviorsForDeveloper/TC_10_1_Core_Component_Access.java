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
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    private class RandomCoreComponentContainer {
        private AppUserObject appUser;
        private int yieldPointer = 0;
        int numberOfWIPCCs;
        int numberOfDraftCCs;
        int numberOfCandidateCCs;

        private List<ACCObject> arrayWIPACCs = new ArrayList<>();
        private List<ASCCPObject> arrayWIPASCCPs = new ArrayList<>();
        private List<BCCPObject> arrayWIPBCCPs = new ArrayList<>();
        private List<ACCObject> arrayDraftACCs = new ArrayList<>();
        private List<ASCCPObject> arrayDraftASCCPs = new ArrayList<>();
        private List<BCCPObject> arrayDraftBCCPs = new ArrayList<>();

        private List<ACCObject> arrayCandidateACCs = new ArrayList<>();
        private List<ASCCPObject> arrayCandidateASCCPs = new ArrayList<>();
        private List<BCCPObject> arrayCandidateBCCPs = new ArrayList<>();
        public RandomCoreComponentContainer(AppUserObject appUser, ReleaseObject release,NamespaceObject namespace) {
            this(appUser, release, namespace, nextInt(1, 3), nextInt(1, 3), nextInt(1, 3));
        }

        public RandomCoreComponentContainer(AppUserObject appUser, ReleaseObject release, NamespaceObject namespace,
                                           int numberOfWIPCCs, int numberOfDraftCCs, int numberOfCandidateCCs) {
            this.appUser = appUser;
            this.numberOfWIPCCs = numberOfWIPCCs;
            this.numberOfDraftCCs = numberOfDraftCCs;
            this.numberOfCandidateCCs = numberOfCandidateCCs;

            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
            thisAccountWillBeDeletedAfterTests(developer);

            for (int i = 0; i < numberOfWIPCCs; ++i) {
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
                    arrayWIPACCs.add(acc);
                    arrayWIPASCCPs.add(asccp);
                    arrayWIPBCCPs.add(bccp);
                }
            }
            for (int i = 0; i < numberOfDraftCCs; ++i) {
                ASCCPObject asccp;
                BCCPObject bccp;
                ACCObject acc;

                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

                    acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Draft");
                    DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
                    bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Draft");
                    BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Draft");
                    bcc.setCardinalityMax(1);
                    coreComponentAPI.updateBCC(bcc);

                    ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Draft");
                    BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Draft");
                    coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Draft");

                    asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Draft");
                    ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Draft");
                    ascc.setCardinalityMax(1);
                    coreComponentAPI.updateASCC(ascc);
                    arrayDraftACCs.add(acc);
                    arrayDraftASCCPs.add(asccp);
                    arrayDraftBCCPs.add(bccp);
                }

            }
            for (int i = 0; i < numberOfCandidateCCs; ++i) {
                ASCCPObject asccp;
                BCCPObject bccp;
                ACCObject acc;

                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

                    acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Candidate");
                    DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
                    bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Candidate");
                    BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Candidate");
                    bcc.setCardinalityMax(1);
                    coreComponentAPI.updateBCC(bcc);

                    ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Candidate");
                    BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Candidate");
                    coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Candidate");

                    asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Candidate");
                    ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Candidate");
                    ascc.setCardinalityMax(1);
                    coreComponentAPI.updateASCC(ascc);
                    arrayCandidateACCs.add(acc);
                    arrayCandidateASCCPs.add(asccp);
                    arrayCandidateBCCPs.add(bccp);
                }

            }
        }

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

            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
            thisAccountWillBeDeletedAfterTests(developer);

            for (int i = 0; i < this.states.size(); ++i) {
                ASCCPObject asccp;
                BCCPObject bccp;
                ACCObject acc;
                String state = this.states.get(i);

                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

                    acc = coreComponentAPI.createRandomACC(developer, release, namespace, state);
                    DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
                    bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, state);
                    BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, state);
                    bcc.setCardinalityMax(1);
                    coreComponentAPI.updateBCC(bcc);

                    ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, state);
                    BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, state);
                    coreComponentAPI.appendBCC(acc_association, bccp_to_append, state);

                    asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, state);
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

        RandomCoreComponentContainer randomCoreComponentContainer = new RandomCoreComponentContainer(developer, release, namespace);

        AppUserObject developer2 = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developer2);

        HomePage homePage = loginPage().signIn(developer2.getLoginId(), developer2.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        int numOfWIPACCs = randomCoreComponentContainer.arrayWIPACCs.size();
        for(int i=0; i < numOfWIPACCs; i++){
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(randomCoreComponentContainer.arrayWIPACCs.get(i).getDen());
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());
        }
        int numOfWIPASCCPs = randomCoreComponentContainer.arrayWIPASCCPs.size();
        for(int i=0; i < numOfWIPASCCPs; i++){
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(randomCoreComponentContainer.arrayWIPASCCPs.get(i).getDen());
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());
        }

        int numOfWIPBCCPs = randomCoreComponentContainer.arrayWIPBCCPs.size();
        for(int i=0; i < numOfWIPBCCPs; i++){
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(randomCoreComponentContainer.arrayWIPBCCPs.get(i).getDen());
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());
        }

        int numOfDraftACCs = randomCoreComponentContainer.arrayDraftACCs.size();
        for(int i=0; i < numOfDraftACCs; i++){
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(randomCoreComponentContainer.arrayDraftACCs.get(i).getDen());
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());
        }

        int numOfDraftASCCPs = randomCoreComponentContainer.arrayDraftASCCPs.size();
        for(int i=0; i < numOfDraftASCCPs; i++){
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(randomCoreComponentContainer.arrayDraftASCCPs.get(i).getDen());
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());
        }

        int numOfDraftBCCPs = randomCoreComponentContainer.arrayDraftBCCPs.size();
        for(int i=0; i < numOfDraftBCCPs; i++){
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(randomCoreComponentContainer.arrayDraftBCCPs.get(i).getDen());
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());
        }

        int numOfCandidateACCs = randomCoreComponentContainer.arrayCandidateACCs.size();
        for(int i=0; i < numOfCandidateACCs; i++){
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(randomCoreComponentContainer.arrayCandidateACCs.get(i).getDen());
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());
        }
        int numOfCandidateASCCPs = randomCoreComponentContainer.arrayCandidateASCCPs.size();
        for(int i=0; i < numOfCandidateASCCPs; i++){
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(randomCoreComponentContainer.arrayCandidateASCCPs.get(i).getDen());
            assertTrue(viewEditCoreComponentPage.getTableRecordAtIndex(1).isDisplayed());
        }
        int numOfCandidateBCCPs = randomCoreComponentContainer.arrayCandidateBCCPs.size();
        for(int i=0; i < numOfCandidateBCCPs; i++){
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setDEN(randomCoreComponentContainer.arrayCandidateBCCPs.get(i).getDen());
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
        assertEnabled(accViewEditPage.getCoreComponentTypeField());

        accViewEditPage.openPage(); // refresh the page to erase the snackbar message
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
        assertEquals("WIP", getText(bccPanelContainer.getBCCPanel().getStateField()));
        assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
        assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
        assertEnabled(bccPanelContainer.getBCCPanel().getPropertyTermField());
        assertEnabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
        assertEnabled(bccPanelContainer.getBCCPanel().getDefinitionField());
        assertEnabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
        assertEnabled(bccPanelContainer.getBCCPanel().getNamespaceSelectField());

        accViewEditPage.openPage();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanelContainer asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);
        asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        asccPanelContainer = accViewEditPage.getASCCPanelContainer(asccNode);

        assertEquals("WIP", getText(asccPanelContainer.getASCCPanel().getStateField()));
        assertDisabled(asccPanelContainer.getASCCPanel().getStateField());
        assertDisabled(asccPanelContainer.getASCCPanel().getGUIDField());
        assertDisabled(asccPanelContainer.getASCCPanel().getDENField());
        assertEnabled(asccPanelContainer.getASCCPPanel().getPropertyTermField());
        assertEnabled(bccPanelContainer.getBCCPanel().getDefinitionField());
        assertEnabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
        assertEnabled(bccPanelContainer.getBCCPanel().getNamespaceSelectField());
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

        By COMMENT_FIELD_LOCATOR =
                By.xpath("//span[contains(text(), \"Comment\")]//ancestor::mat-form-field//textarea");

        assertEquals("WIP", getText(accViewEditPage.getStateField()));
        assertDisabled(accViewEditPage.getStateField());
        assertDisabled(accViewEditPage.getGUIDField());
        assertDisabled(accViewEditPage.getDENField());
        assertDisabled(accViewEditPage.getObjectClassTermField());
        assertDisabled(accViewEditPage.getDefinitionField());
        assertDisabled(accViewEditPage.getDefinitionSourceField());
        assertDisabled(accViewEditPage.getNamespaceField());
        assertDisabled(accViewEditPage.getCoreComponentTypeField());
        click(accViewEditPage.getCommentsIcon());
        assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));


        accViewEditPage.openPage(); // refresh the page to erase the snackbar message
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
        assertEquals("WIP", getText(bccPanelContainer.getBCCPanel().getStateField()));
        assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
        assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
        assertDisabled(bccPanelContainer.getBCCPanel().getPropertyTermField());
        assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
        assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
        assertDisabled(bccPanelContainer.getBCCPanel().getNamespaceSelectField());
        click(bccPanelContainer.getBCCPanel().getCommentsIcon());
        assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));

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
        click(asccPanelContainer.getASCCPPanel().getCommentsIcon());
        assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));

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
        ccStates.add("Release Draft");

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

            By COMMENT_FIELD_LOCATOR =
                    By.xpath("//span[contains(text(), \"Comment\")]//ancestor::mat-form-field//textarea");

            assertEquals(state, getText(accViewEditPage.getStateField()));
            assertDisabled(accViewEditPage.getStateField());
            assertDisabled(accViewEditPage.getGUIDField());
            assertDisabled(accViewEditPage.getDENField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getNamespaceField());
            assertDisabled(accViewEditPage.getCoreComponentTypeField());
            click(accViewEditPage.getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));


            accViewEditPage.openPage(); // refresh the page to erase the snackbar message
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getPropertyTermField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
            assertDisabled(bccPanelContainer.getBCCPanel().getNamespaceSelectField());
            click(bccPanelContainer.getBCCPanel().getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));

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
            click(asccPanelContainer.getASCCPPanel().getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));
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

            By COMMENT_FIELD_LOCATOR =
                    By.xpath("//span[contains(text(), \"Comment\")]//ancestor::mat-form-field//textarea");

            assertEquals(state, getText(accViewEditPage.getStateField()));
            assertDisabled(accViewEditPage.getStateField());
            assertDisabled(accViewEditPage.getGUIDField());
            assertDisabled(accViewEditPage.getDENField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getNamespaceField());
            assertDisabled(accViewEditPage.getCoreComponentTypeField());
            click(accViewEditPage.getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));


            accViewEditPage.openPage(); // refresh the page to erase the snackbar message
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getPropertyTermField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
            assertDisabled(bccPanelContainer.getBCCPanel().getNamespaceSelectField());
            click(bccPanelContainer.getBCCPanel().getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));

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
            click(asccPanelContainer.getASCCPPanel().getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));
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

            By COMMENT_FIELD_LOCATOR =
                    By.xpath("//span[contains(text(), \"Comment\")]//ancestor::mat-form-field//textarea");

            assertEquals(state, getText(accViewEditPage.getStateField()));
            assertDisabled(accViewEditPage.getStateField());
            assertDisabled(accViewEditPage.getGUIDField());
            assertDisabled(accViewEditPage.getDENField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getNamespaceField());
            assertDisabled(accViewEditPage.getCoreComponentTypeField());
            click(accViewEditPage.getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));


            accViewEditPage.openPage(); // refresh the page to erase the snackbar message
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getPropertyTermField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
            assertDisabled(bccPanelContainer.getBCCPanel().getNamespaceSelectField());
            click(bccPanelContainer.getBCCPanel().getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));

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
            click(asccPanelContainer.getASCCPPanel().getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));
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

            By COMMENT_FIELD_LOCATOR =
                    By.xpath("//span[contains(text(), \"Comment\")]//ancestor::mat-form-field//textarea");

            assertEquals(state, getText(accViewEditPage.getStateField()));
            assertDisabled(accViewEditPage.getStateField());
            assertDisabled(accViewEditPage.getGUIDField());
            assertDisabled(accViewEditPage.getDENField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getNamespaceField());
            assertDisabled(accViewEditPage.getCoreComponentTypeField());
            click(accViewEditPage.getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));


            accViewEditPage.openPage(); // refresh the page to erase the snackbar message
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getPropertyTermField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
            assertDisabled(bccPanelContainer.getBCCPanel().getNamespaceSelectField());
            click(bccPanelContainer.getBCCPanel().getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));

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
            click(asccPanelContainer.getASCCPPanel().getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));
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

            By COMMENT_FIELD_LOCATOR =
                    By.xpath("//span[contains(text(), \"Comment\")]//ancestor::mat-form-field//textarea");

            assertEquals(state, getText(accViewEditPage.getStateField()));
            assertDisabled(accViewEditPage.getStateField());
            assertDisabled(accViewEditPage.getGUIDField());
            assertDisabled(accViewEditPage.getDENField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getNamespaceField());
            assertDisabled(accViewEditPage.getCoreComponentTypeField());
            click(accViewEditPage.getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));


            accViewEditPage.openPage(); // refresh the page to erase the snackbar message
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(state, getText(bccPanelContainer.getBCCPanel().getStateField()));
            assertDisabled(bccPanelContainer.getBCCPPanel().getStateField());
            assertDisabled(bccPanelContainer.getBCCPanel().getGUIDField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDENField());
            assertDisabled(bccPanelContainer.getBCCPanel().getPropertyTermField());
            assertDisabled(bccPanelContainer.getBCCPanel().getValueConstraintSelectField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionField());
            assertDisabled(bccPanelContainer.getBCCPanel().getDefinitionSourceField());
            assertDisabled(bccPanelContainer.getBCCPanel().getNamespaceSelectField());
            click(bccPanelContainer.getBCCPanel().getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));

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
            click(asccPanelContainer.getASCCPPanel().getCommentsIcon());
            assertEnabled(visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR));
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
            viewEditCoreComponentPage.getTypeSelectField().click();
            List<WebElement> options = getDriver().findElements(By.cssSelector("mat-option"));
            for (String ccState : Arrays.asList("ACC","ASCCP", "BCCP", "CDT", "BDT" )){
                List<WebElement> result = options.stream().filter(e -> ccState.equals(getText(e))).collect(Collectors.toList());
                result.get(0).click();
            }
            // search by "ACC" type
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.getTypeSelectField().click();
            List<WebElement> accOption = options.stream().filter(e -> "ACC".equals(getText(e))).collect(Collectors.toList());
            accOption.get(0).click();
            click(viewEditCoreComponentPage.getSearchButton());
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(acc.getDen(), developer.getLoginId()).isDisplayed());

            // search by "ASCCP" type
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.getTypeSelectField().click();
            List<WebElement> asccpOption = options.stream().filter(e -> "ASCCP".equals(getText(e))).collect(Collectors.toList());
            asccpOption.get(0).click();
            click(viewEditCoreComponentPage.getSearchButton());
            assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner(asccp.getDen(), developer.getLoginId()).isDisplayed());

            // search by "BCCP" type
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.getTypeSelectField().click();
            List<WebElement> bccpOption = options.stream().filter(e -> "BCCP".equals(getText(e))).collect(Collectors.toList());
            bccpOption.get(0).click();
            click(viewEditCoreComponentPage.getSearchButton());
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
            viewEditCoreComponentPage.getStateSelectField().click();
            List<WebElement> options = getDriver().findElements(By.cssSelector("mat-option"));

            // search by state
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.getStateSelectField().click();
            List<WebElement> stateOption = options.stream().filter(e -> state.equals(getText(e))).collect(Collectors.toList());
            stateOption.get(0).click();
            click(viewEditCoreComponentPage.getSearchButton());
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
            LocalDateTime creationTime = LocalDateTime.of(
                    2018,
                    RandomUtils.nextInt(1, 13),
                    RandomUtils.nextInt(1, 29),
                    RandomUtils.nextInt(0, 24),
                    RandomUtils.nextInt(0, 60),
                    RandomUtils.nextInt(0, 60)
            );
            LocalDateTime updateTime = LocalDateTime.of(
                    2019,
                    RandomUtils.nextInt(1, 13),
                    RandomUtils.nextInt(1, 29),
                    RandomUtils.nextInt(0, 24),
                    RandomUtils.nextInt(0, 60),
                    RandomUtils.nextInt(0, 60)
            );
            acc.setCreationTimestamp(creationTime);
            acc.setLastUpdateTimestamp(updateTime);
            asccp.setCreationTimestamp(creationTime);
            asccp.setLastUpdateTimestamp(updateTime);
            bccp.setCreationTimestamp(creationTime);
            bccp.setLastUpdateTimestamp(updateTime);

            // search by Updated date
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setUpdatedStartDate(creationTime);
            viewEditCoreComponentPage.setUpdatedEndDate(updateTime);
            click(viewEditCoreComponentPage.getSearchButton());
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
        click(viewEditCoreComponentPage.getSearchButton());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("Action Code. Code", "oagis").isDisplayed());
        assertFalse(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("Corrective Action Type Code. Code", "oagis").isDisplayed());
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
        click(viewEditCoreComponentPage.getSearchButton());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("ASN Reference. Document Reference", "oagis").isDisplayed());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("Show Receive Delivery. Show Receive Delivery", "oagis").isDisplayed());

        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setDefinition("\"Notice Document\"");
        click(viewEditCoreComponentPage.getSearchButton());
        assertTrue(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("ASN Reference. Document Reference", "oagis").isDisplayed());
        assertFalse(viewEditCoreComponentPage.getTableRecordByCCNameAndOwner("Show Receive Delivery. Show Receive Delivery", "oagis").isDisplayed());
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
        click(viewEditCoreComponentPage.getSearchButton());
        WebElement searchByModule = getDriver().findElement(By.xpath("//*[contains(text(),\"Model\\Platform\\2_6\\Common\\Components\\Components\")]"));
        assertEquals(0, (getDriver().findElement(By.xpath("//*[contains(text(),\"Model\\OAGIS-Nouns\")]"))).getSize());

        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setModule("Master");
        viewEditCoreComponentPage.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-chip[.=\"BCC\"]")).size());
    }

    @Test
    @DisplayName("TC_10_1_TA_17")
    public void test_TA_17(){

    }

    @Test
    @DisplayName("TC_10_1_TA_18")
    public void test_TA_18(){

    }

    @Test
    @DisplayName("TC_10_1_TA_19")
    public void test_TA_19(){

    }

    @Test
    @DisplayName("TC_10_1_TA_20")
    public void test_TA_20(){

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
