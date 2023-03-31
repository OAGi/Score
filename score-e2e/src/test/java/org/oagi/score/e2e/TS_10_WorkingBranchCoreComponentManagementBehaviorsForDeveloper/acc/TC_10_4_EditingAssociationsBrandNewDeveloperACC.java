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
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_4_EditingAssociationsBrandNewDeveloperACC extends BaseTest {
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
    public void test_TA_10_4_1_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Published");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            ASCCPObject asccp;
            WebElement asccNode;
            String state = entry.getKey();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
            appendASCCPDialog.selectAssociation(asccp.getDen());

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPPanel asccpPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
            assertEquals(state, getText(asccpPanel.getStateField()));
        }
    }

    @Test
    public void test_TA_10_4_1_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc_association = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "Published");
        viewEditCoreComponentPage.openPage();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitReviseButton();
        asccpViewEditPage.toggleDeprecated();
        asccpViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.setDEN(asccp.getDen());
        appendASCCPDialog.hitSearchButton();
        By APPEND_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Append\")]//ancestor::button[1]");

        WebElement tr;
        WebElement td;
        try {
            tr = visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + 1 + "]"));
            td = tr.findElement(By.className("mat-column-" + "den"));
        } catch (TimeoutException e) {
            throw new NoSuchElementException("Cannot locate an association using " + asccp.getDen(), e);
        }
        click(tr.findElement(By.className("mat-column-" + "select")));
        click(elementToBeClickable(getDriver(), APPEND_BUTTON_LOCATOR));

        assertEquals("Confirmation required", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//score-confirm-dialog//div[contains(@class, \"header\")]"))));
    }

    @Test
    public void test_TA_10_4_1_c() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation("Account Identifiers. Named Identifiers");
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation("Account Identifiers. Named Identifiers");
        assertTrue(getSnackBarMessage(getDriver()).contains("already has ASCCP [Account Identifiers. Named Identifiers]"));

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Account Identifiers");
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();

        assertEquals("0", getText(asccPanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(asccPanel.getCardinalityMaxField()));
        assertNotChecked(asccPanel.getDeprecatedCheckbox());
        assertDisabled(asccPanel.getDeprecatedCheckbox());
    }
    @Test
    public void test_TA_10_4_1_d() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation("Account Identifiers. Named Identifiers");

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Account Identifiers");
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();

        assertEquals("WIP", getText(asccPanel.getStateField()));
    }

    @Test
    public void test_TA_10_4_1_e() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, acc_with_nonReusableASCCP;
        ASCCPObject asccp_NotReusable;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            acc_with_nonReusableASCCP = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            asccp_NotReusable = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc_with_nonReusableASCCP, asccp_NotReusable, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp_NotReusable.getAsccpManifestId());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
        asccpPanel.toggleReusable();
        asccpViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.setDEN(asccp_NotReusable.getDen());
        appendASCCPDialog.hitSearchButton();
        By APPEND_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Append\")]//ancestor::button[1]");

        WebElement tr;
        WebElement td;
        try {
            tr = visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + 1 + "]"));
            td = tr.findElement(By.className("mat-column-" + "den"));
        } catch (TimeoutException e) {
            throw new NoSuchElementException("Cannot locate an association using " + asccp_NotReusable.getDen(), e);
        }
        click(tr.findElement(By.className("mat-column-" + "select")));
        click(elementToBeClickable(getDriver(), APPEND_BUTTON_LOCATOR));
        assertTrue("Target ASCCP is not reusable.".equals(getSnackBarMessage(getDriver())));

        // Also test for when non-reusable ASCCP has been deleted while still having an association and
        // the developer still try to use the ASCCP in another association.
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp_NotReusable.getDen());

        //delete the asccp_NotReusable
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp_NotReusable.getAsccpManifestId());
        asccpViewEditPage.hitDeleteButton();

        //Verify that the asccp_NotReusable is still in assocation and in "Deleted" state
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc_with_nonReusableASCCP.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc_with_nonReusableASCCP.getDen() + "/" + asccp_NotReusable.getPropertyTerm());
        ACCViewEditPage.ASCCPPanel asccpPanel2 = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals("Deleted", getText(asccpPanel2.getStateField()));
        assertNotChecked(asccpPanel2.getReusableCheckbox());
        assertDisabled(asccpPanel2.getReusableCheckbox());
    }

    @Test
    public void test_TA_10_4_1_f() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        NamespaceObject enduserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        ACCObject acc_endUser = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, enduserNamespace, "WIP");
        ASCCPObject asccp_endUser = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_endUser, endUser, enduserNamespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.setDEN(asccp_endUser.getDen());
        appendASCCPDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"" + asccp_endUser.getPropertyTerm() + "\")]//ancestor::tr/td[1]//label/span[1]")).size());
    }
    @Test
    public void test_TA_10_4_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc_association = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ASCCPObject asccp, asccp_before, asccp_after;
        asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");
        asccp_after = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");
        asccp_before = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        appendASCCPDialog = accViewEditPage.insertPropertyBefore("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        appendASCCPDialog.selectAssociation(asccp_before.getDen());

        appendASCCPDialog = accViewEditPage.insertPropertyAfter("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        appendASCCPDialog.selectAssociation(asccp_after.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp_before.getPropertyTerm());
        ACCViewEditPage.ASCCPPanel asccp_before_panel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals(asccp_before.getDen(), getText(asccp_before_panel.getDENField()));

        asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp_after.getPropertyTerm());
        ACCViewEditPage.ASCCPPanel asccp_after_panel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals(asccp_after.getDen(), getText(asccp_after_panel.getDENField()));
    }

    @Test
    public void test_TA_10_4_3_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
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

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("0", getText(asccPanel.getCardinalityMinField()));

        asccPanel.setCardinalityMinField("-1");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"is not allowed for Cardinality Min\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));
    }

    @Test
    public void test_TA_10_4_3_b() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
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
            ascc.setDefinition(null);
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("1", getText(asccPanel.getCardinalityMaxField()));
        asccPanel.setCardinalityMaxField("-10");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"not allowed for Cardinality Max\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));

        //check max greater than min
        asccPanel.setCardinalityMinField("111");
        asccPanel.setCardinalityMaxField("11");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(),\"must be greater than\")]")).size());
        click(accViewEditPage.getUpdateButton(true));
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//score-confirm-dialog//div[contains(@class, \"header\")]"))));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update anyway\")]//ancestor::button[1]")));

        assertEquals("111", getText(asccPanel.getCardinalityMinField()));
    }

    @Test
    public void test_TA_10_4_3_c() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
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

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("1", getText(asccPanel.getCardinalityMaxField()));
        asccPanel.setCardinalityMaxField("-1");
        waitFor(ofMillis(500L));
        assertEquals("unbounded", getText(asccPanel.getCardinalityMaxField()));
    }

    @Test
    public void test_TA_10_4_3_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
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

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("0", getText(asccPanel.getCardinalityMinField()));
        asccPanel.getCardinalityMinField().clear();
        assertEquals("true", asccPanel.getCardinalityMinField().getAttribute("aria-required"));

        assertEquals("1", getText(asccPanel.getCardinalityMaxField()));
        asccPanel.getCardinalityMaxField().clear();
        assertEquals("true", asccPanel.getCardinalityMaxField().getAttribute("aria-required"));
    }

    @Test
    public void test_TA_10_4_3_e() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
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
            ascc.setDefinition(null);
            coreComponentAPI.updateASCC(ascc);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();

        asccPanel.setCardinalityMinField("11");
        asccPanel.setCardinalityMaxField("111");
        click(accViewEditPage.getUpdateButton(true));
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//score-confirm-dialog//div[contains(@class, \"header\")]"))));
    }

    @Test
    public void test_TA_10_4_3_f() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
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

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertNotChecked(asccPanel.getDeprecatedCheckbox());
        assertDisabled(asccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_4_4_a() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Published");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            BCCPObject bccp;
            WebElement bccNode;
            String state = entry.getKey();
            bccp = randomCoreComponentWithStateContainer.stateBCCPs.get(state);

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
            appendBCCPDialog.selectAssociation(bccp.getDen());

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPPanel bccpPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPPanel();
            assertEquals(state, getText(bccpPanel.getStateField()));
        }

    }

    @Test
    public void test_TA_10_4_4_b() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp_to_append.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        bccpPanel.toggleDeprecated();
        bccpViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.setDEN(bccp_to_append.getDen());
        appendBCCPDialog.hitSearchButton();
        By APPEND_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Append\")]//ancestor::button[1]");

        WebElement tr;
        WebElement td;
        try {
            tr = visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + 1 + "]"));
            td = tr.findElement(By.className("mat-column-" + "den"));
        } catch (TimeoutException e) {
            throw new NoSuchElementException("Cannot locate an association using " + bccp_to_append.getDen(), e);
        }
        click(tr.findElement(By.className("mat-column-" + "select")));
        click(elementToBeClickable(getDriver(), APPEND_BUTTON_LOCATOR));

        assertEquals("Confirmation required", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//score-confirm-dialog//div[contains(@class, \"header\")]"))));
    }

    @Test
    public void test_TA_10_4_4_c() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation("Accrued Amount");

        appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation("Accrued Amount");

        assertTrue(getSnackBarMessage(getDriver()).contains("already has BCCP [Accrued Amount. Amount]"));

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Accrued Amount");
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(bccPanel.getCardinalityMaxField()));
        assertNotChecked(bccPanel.getDeprecatedCheckbox());
        assertDisabled(bccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_4_4_d() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("WIP", getText(bccPanel.getStateField()));
    }

    @Test
    public void test_TA_10_4_5() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        NamespaceObject enduserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        BCCPObject bccp_endUser;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp_endUser = coreComponentAPI.createRandomBCCP(dataType, endUser, enduserNamespace, "Published");
        }
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.setDEN(bccp_endUser.getDen());
        appendBCCPDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"" + bccp_endUser.getPropertyTerm() + "\")]//ancestor::tr/td[1]//label/span[1]")).size());

        BCCPObject bccp, bccp_before, bccp_after;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            bccp_before = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            bccp_after = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp.getDen());

        appendBCCPDialog = accViewEditPage.insertPropertyBefore("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        appendBCCPDialog.selectAssociation(bccp_before.getDen());

        appendBCCPDialog = accViewEditPage.insertPropertyAfter("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        appendBCCPDialog.selectAssociation(bccp_after.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_before.getPropertyTerm());
        ACCViewEditPage.BCCPPanel bccp_before_panel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPPanel();
        assertEquals(bccp_before.getDen(), getText(bccp_before_panel.getDENField()));

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_after.getPropertyTerm());
        ACCViewEditPage.BCCPPanel bccp_after_panel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPPanel();
        assertEquals(bccp_after.getDen(), getText(bccp_after_panel.getDENField()));
    }

    @Test
    public void test_TA_10_4_6_a() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));

        bccPanel.setCardinalityMinField("-1");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"is not allowed for Cardinality Min\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));

        bccPanel.setCardinalityMinField("10");
        bccPanel.setDefinition("test cardinality min >=0");
        accViewEditPage.hitUpdateButton();
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("10", getText(bccPanel.getCardinalityMinField()));
    }

    @Test
    public void test_TA_10_4_6_b() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));

        bccPanel.setCardinalityMaxField("-9");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"is not allowed for Cardinality Max\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));

        bccPanel.setCardinalityMaxField("11");
        bccPanel.setCardinalityMinField("111");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"must be less than or equal\")]")).size());
        bccPanel.setDefinition("Test Max >= Min");
        accViewEditPage.hitUpdateButton();
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("11", getText(bccPanel.getCardinalityMaxField()));
    }

    @Test
    public void test_TA_10_4_6_c() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("unbounded", getText(bccPanel.getCardinalityMaxField()));
        bccPanel.setCardinalityMaxField("-1");
        waitFor(ofMillis(500L));
        assertEquals("unbounded", getText(bccPanel.getCardinalityMaxField()));

    }

    @Test
    public void test_TA_10_4_6_d() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));
        bccPanel.getCardinalityMinField().clear();
        assertEquals("true", bccPanel.getCardinalityMinField().getAttribute("aria-required"));

        assertEquals("unbounded", getText(bccPanel.getCardinalityMaxField()));
        bccPanel.getCardinalityMaxField().clear();
        assertEquals("true", bccPanel.getCardinalityMaxField().getAttribute("aria-required"));

        bccPanel.setCardinalityMinField("11");
        bccPanel.setCardinalityMaxField("111");
        click(accViewEditPage.getUpdateButton(true));
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//score-confirm-dialog//div[contains(@class, \"header\")]"))));

    }

    @Test
    public void test_TA_10_4_6_e() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertNotChecked(bccPanel.getDeprecatedCheckbox());
        assertDisabled(bccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_4_6_f() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("Element", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("None", getText(bccPanel.getValueConstraintSelectField()));
        assertDisabled(bccPanel.getValueConstraintSelectField());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation("Record Set Total");

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Record Set Total");
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("Element", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("None", getText(bccPanel.getValueConstraintSelectField()));
        assertDisabled(bccPanel.getValueConstraintSelectField());
        bccPanel.setEntityType("Attribute");
        assertEnabled(bccPanel.getValueConstraintSelectField());
        bccPanel.setValueConstraint("Default Value");
        bccPanel.setDefaultValue("99");
        bccPanel.setDefinition("test");
        accViewEditPage.hitUpdateButton();

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Record Set Total");
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("Attribute", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("Default Value", getText(bccPanel.getValueConstraintSelectField()));
        assertEquals("99", getText(bccPanel.getDefaultValueField()));
    }

    @Test
    public void test_TA_10_4_6_g_h() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("Element", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("None", getText(bccPanel.getValueConstraintSelectField()));
        assertDisabled(bccPanel.getValueConstraintSelectField());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation("Open Invoice Count. Number");

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Open Invoice Count");
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("Element", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("None", getText(bccPanel.getValueConstraintSelectField()));
        assertDisabled(bccPanel.getValueConstraintSelectField());
        bccPanel.setEntityType("Attribute");
        assertEnabled(bccPanel.getValueConstraintSelectField());
        bccPanel.setDefinition("test");
        accViewEditPage.hitUpdateButton();

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Open Invoice Count");
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("Attribute", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("0", getText(bccPanel.getCardinalityMinField()));
        assertEquals("1", getText(bccPanel.getCardinalityMaxField()));

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation("Confirmation Code. Code");

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Confirmation Code");
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("Element", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("None", getText(bccPanel.getValueConstraintSelectField()));
        assertDisabled(bccPanel.getEntityTypeSelectField());
    }

    @Test
    public void test_TA_10_4_7() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc_association = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ASCCPObject asccp, asccp_before, asccp_after;
        asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");
        asccp_after = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");
        asccp_before = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        appendASCCPDialog = accViewEditPage.insertPropertyBefore("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        appendASCCPDialog.selectAssociation(asccp_before.getDen());

        appendASCCPDialog = accViewEditPage.insertPropertyAfter("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        appendASCCPDialog.selectAssociation(asccp_after.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp_before.getPropertyTerm());
        ACCViewEditPage.ASCCPPanel asccp_before_panel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals(asccp_before.getDen(), getText(asccp_before_panel.getDENField()));

        asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp_after.getPropertyTerm());
        ACCViewEditPage.ASCCPPanel asccp_after_panel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals(asccp_after.getDen(), getText(asccp_after_panel.getDENField()));
    }

    @Test
    public void test_TA_10_4_8() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        NamespaceObject enduserNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.enduser.test");
        BCCPObject bccp_endUser;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp_endUser = coreComponentAPI.createRandomBCCP(dataType, endUser, enduserNamespace, "Published");
        }
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.setDEN(bccp_endUser.getDen());
        appendBCCPDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"" + bccp_endUser.getPropertyTerm() + "\")]//ancestor::tr/td[1]//label/span[1]")).size());

        BCCPObject bccp, bccp_before, bccp_after;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            bccp_before = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            bccp_after = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp.getDen());

        appendBCCPDialog = accViewEditPage.insertPropertyBefore("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        appendBCCPDialog.selectAssociation(bccp_before.getDen());

        appendBCCPDialog = accViewEditPage.insertPropertyAfter("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        appendBCCPDialog.selectAssociation(bccp_after.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_before.getPropertyTerm());
        ACCViewEditPage.BCCPPanel bccp_before_panel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPPanel();
        assertEquals(bccp_before.getDen(), getText(bccp_before_panel.getDENField()));

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_after.getPropertyTerm());
        ACCViewEditPage.BCCPPanel bccp_after_panel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPPanel();
        assertEquals(bccp_after.getDen(), getText(bccp_after_panel.getDENField()));
    }

    @Test
    public void test_TA_10_4_9() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc_association = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ASCCPObject asccp, asccp_before, asccp_after;
        asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");
        asccp_after = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");
        asccp_before = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        appendASCCPDialog = accViewEditPage.insertPropertyBefore("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        appendASCCPDialog.selectAssociation(asccp_before.getDen());

        appendASCCPDialog = accViewEditPage.insertPropertyAfter("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        appendASCCPDialog.selectAssociation(asccp_after.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.removeAssociation("/" + acc.getDen() + "/" + asccp.getPropertyTerm());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        String xpathExpr = "//cdk-virtual-scroll-viewport//div//span[contains(@class, \"search-index\")]//*[contains(text(),\"" + asccp.getPropertyTerm() + "\")]";
        assertEquals(0, getDriver().findElements(By.xpath(xpathExpr)).size());
    }

    @Test
    public void test_TA_10_4_10_a() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            WebElement accBaseNode;
            ACCViewEditPage.ACCPanel accBasePanel;

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
            accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
            assertEquals(accForBase.getDen(), getText(accBasePanel.getDENField()));
            accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + accForBase.getDen());

            String state = entry.getKey();
            accForBase = randomCoreComponentWithStateContainer.stateACCs.get(state);

            accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
            accViewEditPage = accSetBaseACCDialog.hitApplyButton(accForBase.getDen());
            accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
            accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
            assertEquals(accForBase.getDen(), getText(accBasePanel.getDENField()));
            assertEquals(state, getText(accBasePanel.getStateField()));
        }
    }

    @Test
    public void test_TA_10_4_10_b() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());

        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals(accForBase.getDen(), getText(accBasePanel.getDENField()));
        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + accForBase.getDen());

        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherDeveloper, release, namespace, "WIP");
        accForBase.setDeprecated(true);
        getAPIFactory().getCoreComponentAPI().updateACC(accForBase);

        accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.setDEN(accForBase.getDen());
        accSetBaseACCDialog.hitSearchButton();
        By APPLY_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Apply\")]//ancestor::button[1]");

        ACCObject finalAccForBase = accForBase;

        WebElement tr;
        WebElement td;
        try {
            tr = visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + 1 + "]"));
            td = tr.findElement(By.className("mat-column-" + "den"));
        } catch (TimeoutException e) {
            throw new NoSuchElementException("Cannot locate an association using " + finalAccForBase.getDen(), e);
        }
        click(tr.findElement(By.className("mat-column-" + "select")));
        click(elementToBeClickable(getDriver(), APPLY_BUTTON_LOCATOR));

        assertEquals("Confirmation required", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//score-confirm-dialog//div[contains(@class, \"header\")]"))));
    }

    @Test
    public void test_TA_10_4_10_c() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());

        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals(accForBase.getDen(), getText(accBasePanel.getDENField()));
        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + accForBase.getDen());

        accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.setDEN("Ledger Amount Group. Details");
        accSetBaseACCDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"Ledger Amount Group. Details\")]//ancestor::tr/td[1]//label/span[1]")).size());
        accSetBaseACCDialog.hitCancelButton();

        accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.setDEN("Any Structured Content. Details");
        accSetBaseACCDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"Any Structured Content. Details\")]//ancestor::tr/td[1]//label/span[1]")).size());
        accSetBaseACCDialog.hitCancelButton();

        accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton("Available Inventory Quantity. Details");
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Available Inventory Quantity. Details");
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals("Semantics", getText(accBasePanel.getComponentTypeSelectField()));
    }

    @Test
    public void test_TA_10_4_10_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());

        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals(accForBase.getDen(), getText(accBasePanel.getDENField()));
        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + accForBase.getDen());

        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherDeveloper, release, namespace, "WIP");

        accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertDisabled(accBasePanel.getObjectClassTermField());
        assertDisabled(accBasePanel.getComponentTypeSelectField());
        assertDisabled(accBasePanel.getAbstractCheckbox());
        assertDisabled(accBasePanel.getDeprecatedCheckbox());
        assertDisabled(accBasePanel.getNamespaceSelectField());
        assertDisabled(accBasePanel.getDefinitionField());
        assertDisabled(accBasePanel.getDefinitionSourceField());
    }

    @Test
    public void test_TA_10_4_10_e() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());

        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals(accForBase.getDen(), getText(accBasePanel.getDENField()));
        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + accForBase.getDen());

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        ACCObject endUser_accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, endUserNamespace, "WIP");

        accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.setDEN(endUser_accForBase.getDen());
        accSetBaseACCDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"" + endUser_accForBase.getDen() + "\")]//ancestor::tr/td[1]//label/span[1]")).size());
    }

    @Test
    public void test_TA_10_4_10_f() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, accForBase, acc_association;
        ASCCObject ascc, asccForBase;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            accForBase = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
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
            asccForBase = coreComponentAPI.appendASCC(accForBase, asccp, "WIP");
            asccForBase.setCardinalityMax(1);
            coreComponentAPI.updateASCC(asccForBase);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.setDEN(accForBase.getDen());
        accSetBaseACCDialog.hitSearchButton();
        By APPLY_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Apply\")]//ancestor::button[1]");

        ACCObject finalAccForBase = accForBase;
        String asccpPropertyTerm = asccp.getPropertyTerm();
        String duplicateWarning = "There is a conflict in ASCCPs between the current ACC and the base ACC";
        WebElement tr;
        WebElement td;
        try {
            tr = visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + 1 + "]"));
            td = tr.findElement(By.className("mat-column-" + "den"));
        } catch (TimeoutException e) {
            throw new NoSuchElementException("Cannot locate an association using " + finalAccForBase.getDen(), e);
        }
        click(tr.findElement(By.className("mat-column-" + "select")));
        click(elementToBeClickable(getDriver(), APPLY_BUTTON_LOCATOR));
        assertTrue(getSnackBarMessage(getDriver()).contains(duplicateWarning));

    }

    @Test
    public void test_TA_10_4_11() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());

        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals(accForBase.getDen(), getText(accBasePanel.getDENField()));
        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + accForBase.getDen());

        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        String xpathExpr = "//cdk-virtual-scroll-viewport//div//span[contains(@class, \"search-index\")]//*[contains(text(),\"" + accForBase.getDen() + "\")]";
        assertEquals(0, getDriver().findElements(By.xpath(xpathExpr)).size());
    }

    @Test
    public void test_TA_10_4_12() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        viewEditCoreComponentPage.openPage();
        {
            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();

            WebElement tr = viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
            WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
            assertTrue(td.findElement(By.tagName("button")).isEnabled());

            TransferCCOwnershipDialog transferCCOwnershipDialog =
                    viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
            transferCCOwnershipDialog.transfer(anotherDeveloper.getLoginId());

            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();

            tr = viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
            td = viewEditCoreComponentPage.getColumnByName(tr, "owner");
            assertEquals(anotherDeveloper.getLoginId(), getText(td));

            //verify the ownership of all associations (ASCC and BCC) are  transferred as well
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
            assertEquals(anotherDeveloper.getLoginId(), getText(asccPanel.getOwnerField()));

            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
            assertEquals(anotherDeveloper.getLoginId(), getText(bccPanel.getOwnerField()));
        }

        homePage.logout();
        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        {
            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();

            WebElement tr = viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
            WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
            assertTrue(td.findElement(By.tagName("button")).isEnabled());

            TransferCCOwnershipDialog transferCCOwnershipDialog =
                    viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
            assertThrows(NoSuchElementException.class, () -> transferCCOwnershipDialog.transfer(endUser.getLoginId()));
        }
    }

    @Test
    public void test_TA_10_4_13() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            accForBase = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());

        {
            String nodePath = "/" + acc.getDen() + "/" + asccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, asccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedASCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + asccp.getPropertyTerm());
            assertTrue(movedASCCPNode.isDisplayed());

        }

        {
            String nodePath = "/" + acc.getDen() + "/" + bccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + bccp.getPropertyTerm());
            assertTrue(movedBCCPNode.isDisplayed());
        }
    }

    @Test
    public void test_TA_10_4_14_abcd() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        String oldBase = null;
        List<String> ccStates = new ArrayList<>();
        ccStates.add("Published");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);
        boolean noBase = false;
        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            accForBase = randomCoreComponentWithStateContainer.stateACCs.get(state);
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            if (noBase){
                accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + oldBase);
            }
            ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
            accSetBaseACCDialog.hitApplyButton(accForBase.getDen());
            oldBase = accForBase.getDen();
            noBase = true;
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            String nodePath = "/" + acc.getDen() + "/" + asccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, asccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertDisabled(selectBaseACCToRefactorDialog.getRefactorButton(false));
        }
    }

    @Test
    public void test_TA_10_4_14_e() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        boolean noBase = false;
        String oldBase = null;

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Published");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(anotherDeveloper, release, namespace, ccStates);

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            accForBase = randomCoreComponentWithStateContainer.stateACCs.get(state);
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            if (noBase){
                accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + oldBase);
            }
            ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
            accSetBaseACCDialog.hitApplyButton(accForBase.getDen());
            oldBase = accForBase.getDen();
            noBase = true;
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            String nodePath = "/" + acc.getDen() + "/" + asccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, asccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertDisabled(selectBaseACCToRefactorDialog.getRefactorButton(false));
        }
    }

    @Test
    public void test_TA_10_4_15_a() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase, random_acc;
        ASCCObject ascc;
        ASCCPObject asccp, random_asccp;
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage;
        SelectAssociationDialog appendAssociationDialog;
        boolean hasBase = false;
        String oldBase = null;

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Published");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            accForBase = randomCoreComponentWithStateContainer.stateACCs.get(state);
            random_acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer,release, namespace, "Published");
            random_asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(random_acc, developer, namespace, "Published");
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            appendAssociationDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
            appendAssociationDialog.selectAssociation(random_asccp.getDen());

            if (hasBase){
                accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + oldBase);
            }
            ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
            accSetBaseACCDialog.hitApplyButton(accForBase.getDen());
            oldBase = accForBase.getDen();
            hasBase = true;
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            String nodePath = "/" + acc.getDen() + "/" + random_asccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, random_asccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertDisabled(selectBaseACCToRefactorDialog.getRefactorButton(false));

            //move accForBase to "WIP" so the "Refactor" will be enabled
            accForBase.setState("WIP");
            getAPIFactory().getCoreComponentAPI().updateACC(accForBase);
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, random_asccp.getPropertyTerm());
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            //Verify the asccp is moved to under the accForBase node
            WebElement movedASCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + random_asccp.getPropertyTerm());
            assertTrue(movedASCCPNode.isDisplayed());
        }
    }

    @Test
    public void test_TA_10_4_15_bc() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherDeveloper, release, namespace, "Published");
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        String nodePath = "/" + acc.getDen() + "/" + asccp.getPropertyTerm();
        SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, asccp.getPropertyTerm());
        WebElement tr;
        tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
        assertTrue(tr.isDisplayed());
        click(tr.findElement(By.className("mat-column-" + "select")));
        selectBaseACCToRefactorDialog.hitAnalyzeButton();
        assertDisabled(selectBaseACCToRefactorDialog.getRefactorButton(false));

        //take the ownership of the accForBase and moved it to "WIP" state for Refactor
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement accNode = accViewEditPage.getNodeByPath("/" + accForBase.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals(developer.getLoginId(), getText(accPanel.getOwnerField()));
        assertEquals("WIP", getText(accPanel.getStateField()));

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, asccp.getPropertyTerm());
        tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
        assertTrue(tr.isDisplayed());
        click(tr.findElement(By.className("mat-column-" + "select")));
        selectBaseACCToRefactorDialog.hitAnalyzeButton();
        assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
        selectBaseACCToRefactorDialog.hitRefactorButton();

        //Verify the asccp is moved to under the accForBase node
        WebElement movedASCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + asccp.getPropertyTerm());
        assertTrue(movedASCCPNode.isDisplayed());
    }
    @Test
    public void test_TA_10_4_16() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        ACCObject acc, acc_association, accForBase, accForBaseLv2, accGroup;
        ASCCObject ascc, asccGroup;
        ASCCPObject asccp, asccpGroup;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
            accForBaseLv2 = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            coreComponentAPI.updateACC(acc);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            coreComponentAPI.appendBCC(acc, bccp_to_append, "WIP");

            accGroup = coreComponentAPI.createRandomACCSemanticGroupType(developer, release, namespace, "WIP");
            coreComponentAPI.appendBCC(accGroup, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);

            asccpGroup = coreComponentAPI.createRandomASCCP(accGroup, developer, namespace, "WIP");
            asccGroup = coreComponentAPI.appendASCC(accForBaseLv2, asccpGroup, "WIP");
            coreComponentAPI.updateASCC(asccGroup);

        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog =  accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());

        //set the base for accForBase
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
        accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + accForBase.getDen());
        accSetBaseACCDialog.hitApplyButton(accForBaseLv2.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());

        String nodePath = "/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm();
        SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp_to_append.getPropertyTerm());

        WebElement tr;
        tr = selectBaseACCToRefactorDialog.getTableRecordByValue(accForBaseLv2.getDen());
        assertTrue(tr.isDisplayed());
        click(tr.findElement(By.className("mat-column-" + "select")));
        selectBaseACCToRefactorDialog.hitAnalyzeButton();
        assertDisabled(selectBaseACCToRefactorDialog.getRefactorButton(false));
        String refactorIssue = "Ungrouping '" + asccpGroup.getPropertyTerm() +"' required.";
        String xpathExpr = "//score-based-acc-dialog//*[contains(text(),\""+refactorIssue+"\")]";
        assertEquals(1, getDriver().findElements(By.xpath(xpathExpr)).size());
    }

    @Test
    public void test_TA_10_4_17() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            accForBase = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.updateBasedACC(acc, accForBase);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            BCCObject bcc = coreComponentAPI.appendBCC(accForBase, bccp, "Published");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp_to_append, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Published");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
        accViewEditPage.hitReviseButton();
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        String nodePath;

        {
            nodePath = "/" + acc.getDen() + "/" + asccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, asccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedASCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + asccp.getPropertyTerm());
            assertTrue(movedASCCPNode.isDisplayed());

        }

        {
            nodePath = "/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp_to_append.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + bccp_to_append.getPropertyTerm());
            assertTrue(movedBCCPNode.isDisplayed());
        }

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitCancelButton();

        {
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedASCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            assertTrue(movedASCCPNode.isDisplayed());

        }

        {
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
            assertTrue(movedBCCPNode.isDisplayed());
        }
    }

    @Test
    public void test_TA_10_4_18() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            accForBase = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.updateBasedACC(acc, accForBase);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(accForBase, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp_to_append, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Published");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
        accViewEditPage.hitReviseButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();

        String nodePath;

        {
            nodePath = "/" + acc.getDen() + "/" + asccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, asccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedASCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + asccp.getPropertyTerm());
            assertTrue(movedASCCPNode.isDisplayed());

        }

        {
            nodePath = "/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp_to_append.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + bccp_to_append.getPropertyTerm());
            assertTrue(movedBCCPNode.isDisplayed());
        }

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitCancelButton();
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
        accViewEditPage.hitCancelButton();

        {
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
            String xpathExpr = "//cdk-virtual-scroll-viewport//div//span[contains(@class, \"search-index\")]//*[contains(text(),\"" + asccp.getPropertyTerm() + "\")]";
            assertEquals(0, getDriver().findElements(By.xpath(xpathExpr)).size());
        }

        {
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
            String xpathExpr = "//cdk-virtual-scroll-viewport//div//span[contains(@class, \"search-index\")]//*[contains(text(),\"" + bccp_to_append.getPropertyTerm() + "\")]";
            assertEquals(0, getDriver().findElements(By.xpath(xpathExpr)).size());
        }
    }

    @Test
    public void test_TA_10_4_19() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            accForBase = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.updateBasedACC(acc, accForBase);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            BCCObject bcc = coreComponentAPI.appendBCC(accForBase, bccp, "Published");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp_to_append, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Published");
            coreComponentAPI.appendASCC(acc, asccp, "Published");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
        accViewEditPage.hitReviseButton();
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();

        String nodePath;

        {
            nodePath = "/" + acc.getDen() + "/" + asccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp_to_append.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + bccp_to_append.getPropertyTerm());
            assertTrue(movedBCCPNode.isDisplayed());

        }

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
        accViewEditPage.hitCancelButton();
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitCancelButton();
    }

    @Test
    public void test_TA_10_4_20() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            accForBase = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.updateBasedACC(acc, accForBase);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            BCCObject bcc = coreComponentAPI.appendBCC(accForBase, bccp, "Published");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp_to_append, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Published");
            coreComponentAPI.appendASCC(acc, asccp, "Published");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
        accViewEditPage.hitReviseButton();
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();

        String nodePath;

        {
            nodePath = "/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp_to_append.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + bccp_to_append.getPropertyTerm());
            assertTrue(movedBCCPNode.isDisplayed());

        }

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(accForBase.getAccManifestId());
        accViewEditPage.hitCancelButton();
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitCancelButton();
    }

    @Test
    public void test_TA_10_4_21_abcd() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Published");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            accForBase = randomCoreComponentWithStateContainer.stateACCs.get(state);
            getAPIFactory().getCoreComponentAPI().updateBasedACC(acc, accForBase);
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            String nodePath = "/" + acc.getDen() + "/" + bccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertDisabled(selectBaseACCToRefactorDialog.getRefactorButton(false));
        }
    }

    @Test
    public void test_TA_10_4_21_e() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Published");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(anotherDeveloper, release, namespace, ccStates);

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            accForBase = randomCoreComponentWithStateContainer.stateACCs.get(state);
            getAPIFactory().getCoreComponentAPI().updateBasedACC(acc, accForBase);
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            String nodePath = "/" + acc.getDen() + "/" + bccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertDisabled(selectBaseACCToRefactorDialog.getRefactorButton(false));
        }
    }

    @Test
    public void test_TA_10_4_22() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            accForBase = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            coreComponentAPI.updateACC(acc);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            BCCObject bcc2 = coreComponentAPI.appendBCC(accForBase, bccp, "WIP");
            coreComponentAPI.updateBCC(bcc2);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;

        getAPIFactory().getCoreComponentAPI().updateBasedACC(acc, accForBase);
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        String nodePath = "/" + acc.getDen() + "/" + bccp.getPropertyTerm();
        SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp.getPropertyTerm());
        WebElement tr;
        tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
        assertTrue(tr.isDisplayed());
        click(tr.findElement(By.className("mat-column-" + "select")));
        selectBaseACCToRefactorDialog.hitAnalyzeButton();
        assertDisabled(selectBaseACCToRefactorDialog.getRefactorButton(false));

    }

    @Test
    public void test_TA_10_4_23() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Classification Base. Details", branch);
        accViewEditPage.hitReviseButton();
        String url = getDriver().getCurrentUrl();
        BigInteger baseACCManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Semantic Classification. Details", branch);
        url = getDriver().getCurrentUrl();
        BigInteger refactorACCManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog selectAssociationDialog = accViewEditPage.appendPropertyAtLast("/Semantic Classification. Details");
        selectAssociationDialog.selectAssociation("Description. Text");

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(refactorACCManifestId);
        String nodePath = "/Semantic Classification. Details/Description";
        SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, "Description");
        WebElement tr;
        tr = selectBaseACCToRefactorDialog.getTableRecordByValue("Classification Base. Details");
        assertTrue(tr.isDisplayed());
        click(tr.findElement(By.className("mat-column-" + "select")));
        selectBaseACCToRefactorDialog.hitAnalyzeButton();
        assertDisabled(selectBaseACCToRefactorDialog.getRefactorButton(false));
        assertEquals(1, getDriver().findElements(By.xpath("//score-based-acc-dialog//*[contains(text(),\"Ungrouping 'Free Form Text Group' required.\")]")).size());
        selectBaseACCToRefactorDialog.hitCancelButton();
    }

    @Test
    public void test_TA_10_4_24_and_26() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            accForBase = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            acc.setBasedAccManifestId(accForBase.getAccManifestId());
            coreComponentAPI.updateACC(acc);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(accForBase, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            coreComponentAPI.appendBCC(acc, bccp_to_append, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();

        String nodePath;
        {
            nodePath = "/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordAtIndex(1);
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen() + "/" + bccp.getPropertyTerm());
            assertTrue(movedBCCPNode.isDisplayed());

            movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            assertFalse(movedBCCPNode.isDisplayed());
        }

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitCancelButton();

        {
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            assertTrue(movedBCCPNode.isDisplayed());
        }

    }

    @Test
    public void test_TA_10_4_25() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage;
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Operation Reference Base. Details", branch);
        accViewEditPage.hitReviseButton();
        String url = getDriver().getCurrentUrl();
        BigInteger baseACCManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Manufacturing Route Operation Base. Details", branch);
        url = getDriver().getCurrentUrl();
        BigInteger refactorACCManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        accViewEditPage.hitReviseButton();
        String nodePath = "/Manufacturing Route Operation Base. Details/Container Identifier";
        SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, "Container Identifier");
        WebElement tr;
        tr = selectBaseACCToRefactorDialog.getTableRecordByValue("Operation Reference Base. Details");
        assertTrue(tr.isDisplayed());
        click(tr.findElement(By.className("mat-column-" + "select")));
        selectBaseACCToRefactorDialog.hitAnalyzeButton();
        assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
        selectBaseACCToRefactorDialog.hitRefactorButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(baseACCManifestId);
        String movedNodePath = "/Operation Reference Base. Details/Container Identifier";
        WebElement movedNode = accViewEditPage.getNodeByPath(movedNodePath);
        assertTrue(movedNode.isDisplayed());

        accViewEditPage.hitCancelButton();
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(baseACCManifestId);
        movedNode = accViewEditPage.getNodeByPath(movedNodePath);
        assertFalse(movedNode.isDisplayed());
    }
}
