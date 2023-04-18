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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_7_EditingAssociationsRevisionDeveloperACC extends BaseTest {

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
    public void test_TA_10_7_1_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Published");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        ASCCPObject asccp;

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            String basePath = "/" + acc.getDen();
            SelectAssociationDialog selectAssociationDialog = accViewEditPage.appendPropertyAtLast(basePath);
            selectAssociationDialog.selectAssociation(asccp.getDen());

            //Verify the asccp is associated
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPPanel asccpPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
            assertEquals(state, getText(asccpPanel.getStateField()));
        }

    }

    @Test
    public void test_TA_10_7_1_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ACCObject acc_association = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "Published");
        viewEditCoreComponentPage.openPage();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitReviseButton();
        asccpViewEditPage.toggleDeprecated();
        asccpViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
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
    public void test_TA_10_7_1_c() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation("Account Identifiers. Named Identifiers");
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation("Account Identifiers. Named Identifiers");
        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//snack-bar-container//score-multi-actions-snack-bar//div[contains(@class, \"header\")]")).isDisplayed();

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("already has ASCCP"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Account Identifiers");
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();

        assertEquals("0", getText(asccPanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(asccPanel.getCardinalityMaxField()));
        assertNotChecked(asccPanel.getDeprecatedCheckbox());
        assertDisabled(asccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_7_1_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ACCObject acc_association = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "Published");

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("WIP", getText(asccPanel.getStateField()));
    }

    @Test
    public void test_TA_10_7_1_e() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        ACCObject acc_WithNonReusableAsccp = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
        ASCCPObject asccp_NotReusable = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
        ASCCObject ascc = coreComponentAPI.appendASCC(acc_WithNonReusableAsccp, asccp_NotReusable, "WIP");
        ascc.setCardinalityMax(1);
        coreComponentAPI.updateASCC(ascc);

        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp_NotReusable.getAsccpManifestId());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
        asccpPanel.toggleReusable();
        asccpViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp_NotReusable.getDen());

        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//snack-bar-container//score-multi-actions-snack-bar//div[contains(@class, \"header\")]")).isDisplayed();

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.equals("Target ASCCP is not reusable."));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));
    }

    @Test
    public void test_TA_10_7_1_f() {

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
        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        ACCObject acc_association = coreComponentAPI.createRandomACC(endUser, release, endUserNamespace, "WIP");
        ASCCPObject asccp_endUser = coreComponentAPI.createRandomASCCP(acc_association, endUser, endUserNamespace, "WIP");
        ASCCObject ascc = coreComponentAPI.appendASCC(acc_association, asccp_endUser, "WIP");
        ascc.setCardinalityMax(1);
        coreComponentAPI.updateASCC(ascc);

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.setDEN(asccp_endUser.getDen());
        appendASCCPDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"" + asccp_endUser.getPropertyTerm() + "\")]//ancestor::tr/td[1]//label/span[1]")).size());
    }

    @Test
    public void test_TA_10_7_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ACCObject acc_association = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ASCCPObject asccp, asccp_before, asccp_after;
        asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");
        asccp_after = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");
        asccp_before = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, developer, namespace, "WIP");

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
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
    public void test_TA_10_7_3_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("0", getText(asccPanel.getCardinalityMinField()));

        asccPanel.setCardinalityMinField("-1");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"is not allowed for Cardinality Min\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));

        asccPanel.setCardinalityMinField("10");
        asccPanel.setDefinition("Test minCardinality must be positive");
        accViewEditPage.hitUpdateButton();
        asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("10", getText(asccPanel.getCardinalityMinField()));

    }


    @Test
    public void test_TA_10_7_3_b() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("unbounded", getText(asccPanel.getCardinalityMaxField()));
        asccPanel.setCardinalityMaxField("-10");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"not allowed for Cardinality Max\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));

        //check max greater than min
        asccPanel.setCardinalityMinField("111");
        asccPanel.setCardinalityMaxField("11");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(),\"Cardinality Max must be greater than\")]")).size());
        click(accViewEditPage.getUpdateButton(true));
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]"))));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update anyway\")]//ancestor::button[1]")));

        assertEquals("111", getText(asccPanel.getCardinalityMinField()));
    }


    @Test
    public void test_TA_10_7_3_c() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("unbounded", getText(asccPanel.getCardinalityMaxField()));
        asccPanel.setCardinalityMaxField("-1");
        waitFor(ofMillis(500L));
        assertEquals("unbounded", getText(asccPanel.getCardinalityMaxField()));
    }

    @Test
    public void test_TA_10_7_3_d() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("0", getText(asccPanel.getCardinalityMinField()));
        asccPanel.getCardinalityMinField().clear();
        assertEquals("true", asccPanel.getCardinalityMinField().getAttribute("aria-required"));

        assertEquals("unbounded", getText(asccPanel.getCardinalityMaxField()));
        asccPanel.getCardinalityMaxField().clear();
        assertEquals("true", asccPanel.getCardinalityMaxField().getAttribute("aria-required"));
    }


    @Test
    public void test_TA_10_7_3_e() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        asccPanel.setCardinalityMinField("11");
        asccPanel.setCardinalityMaxField("111");
        click(accViewEditPage.getUpdateButton(true));
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]"))));

    }

    @Test
    public void test_TA_10_7_3_f() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertNotChecked(asccPanel.getDeprecatedCheckbox());
        assertDisabled(asccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_7_4_a() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(25);
            coreComponentAPI.updateASCC(ascc);

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("2", getText(asccPanel.getRevisionField()));
        asccPanel.setCardinalityMinField("30");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"Min must be less than or equals to\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));
    }

    @Test
    public void test_TA_10_7_4_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMin(75);
            coreComponentAPI.updateASCC(ascc);

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("2", getText(asccPanel.getRevisionField()));
        asccPanel.setCardinalityMaxField("50");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"Max must be greater than\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));
    }

    @Test
    public void test_TA_10_7_4_c_deprecated_in_previous_version() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setDeprecated(true);
            coreComponentAPI.updateASCC(ascc);

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("2", getText(asccPanel.getRevisionField()));
        assertChecked(asccPanel.getDeprecatedCheckbox());
        assertDisabled(asccPanel.getDeprecatedCheckbox());

    }

    @Test
    public void test_TA_10_7_4_c_not_deprecated_in_previous_version() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setDeprecated(false);
            coreComponentAPI.updateASCC(ascc);

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("2", getText(asccPanel.getRevisionField()));
        assertNotChecked(asccPanel.getDeprecatedCheckbox());
        assertEnabled(asccPanel.getDeprecatedCheckbox());
    }


    @Test
    public void test_TA_10_7_4_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            coreComponentAPI.updateASCC(ascc);

        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("2", getText(asccPanel.getRevisionField()));
        //check max greater than min
        asccPanel.setCardinalityMinField("111");
        asccPanel.setCardinalityMaxField("11");
        asccPanel.setDefinition(null);
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(),\"Cardinality Max must be greater than\")]")).size());
        click(accViewEditPage.getUpdateButton(true));
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]"))));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update anyway\")]//ancestor::button[1]")));

        assertEquals("111", getText(asccPanel.getCardinalityMinField()));
    }

    @Test
    public void test_TA_10_7_5_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
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
    public void test_TA_10_7_5_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
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

        waitFor(ofMillis(500));
    }

    @Test
    public void test_TA_10_7_5_c() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation("Accrued Amount");

        appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation("Accrued Amount");
        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//snack-bar-container//score-multi-actions-snack-bar//div[contains(@class, \"header\")]")).isDisplayed();

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("already has BCCP"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//snack-bar-container//span[contains(text(), \"Close\")]//ancestor::button[1]")));

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Accrued Amount");
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(bccPanel.getCardinalityMaxField()));
        assertNotChecked(bccPanel.getDeprecatedCheckbox());
        assertDisabled(bccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_7_5_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("WIP", getText(bccPanel.getStateField()));
    }

    @Test
    public void test_TA_10_7_5_e() {
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
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.setDEN(bccp_endUser.getDen());
        appendBCCPDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"" + bccp_endUser.getPropertyTerm() + "\")]//ancestor::tr/td[1]//label/span[1]")).size());
    }

    @Test
    public void test_TA_10_7_6() {
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
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        BCCPObject bccp, bccp_before, bccp_after;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            bccp_before = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            bccp_after = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
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
    public void test_TA_10_7_7_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));

        bccPanel.setCardinalityMinField("-1");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"is not allowed for Cardinality Min\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));

        bccPanel.setCardinalityMinField("10");
        accViewEditPage.hitUpdateButton();
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("10", getText(bccPanel.getCardinalityMinField()));
    }

    @Test
    public void test_TA_10_7_7_b() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));

        bccPanel.setCardinalityMinField("-9");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"is not allowed for Cardinality Max\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));

        bccPanel.setCardinalityMaxField("11");
        bccPanel.setCardinalityMinField("111");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"must be less than or equal\")]")).size());
        accViewEditPage.hitUpdateButton();
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("11", getText(bccPanel.getCardinalityMaxField()));

    }

    @Test
    public void test_TA_10_7_7_c() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("unbounded", getText(bccPanel.getCardinalityMaxField()));
        bccPanel.setCardinalityMaxField("-1");
        waitFor(ofMillis(500L));
        assertEquals("unbounded", getText(bccPanel.getCardinalityMaxField()));
    }

    @Test
    public void test_TA_10_7_7_d() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));
        bccPanel.getCardinalityMinField().clear();
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"Cardinality Min is required\")]")).isDisplayed());

        assertEquals("unbounded", getText(bccPanel.getCardinalityMaxField()));
        bccPanel.getCardinalityMaxField().clear();
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"Cardinality Max is required\")]")).isDisplayed());

        bccPanel.setCardinalityMinField("11");
        bccPanel.setCardinalityMaxField("111");
        accViewEditPage.hitUpdateButton();
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]"))));

    }

    @Test
    public void test_TA_10_7_7_e() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertNotChecked(bccPanel.getDeprecatedCheckbox());
        assertDisabled(bccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_7_7_f() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

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
    public void test_TA_10_7_7_g() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

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
    public void test_TA_10_7_8_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMin(25);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("Element", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("None", getText(bccPanel.getValueConstraintSelectField()));
        assertDisabled(bccPanel.getValueConstraintSelectField());
        bccPanel.setEntityType("Attribute");
        assertEnabled(bccPanel.getValueConstraintSelectField());
        bccPanel.setDefinition("test");
        accViewEditPage.hitUpdateButton();

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Open Invoice Count");
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("2", getText(bccPanel.getRevisionField()));
        bccPanel.setCardinalityMinField("30");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"Min must be less than or equals to\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));

    }

    @Test
    public void test_TA_10_7_8_b() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMin(25);
            bcc.setCardinalityMax(75);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("Element", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("None", getText(bccPanel.getValueConstraintSelectField()));
        assertDisabled(bccPanel.getValueConstraintSelectField());
        bccPanel.setEntityType("Attribute");
        assertEnabled(bccPanel.getValueConstraintSelectField());
        bccPanel.setDefinition("test");
        accViewEditPage.hitUpdateButton();

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Open Invoice Count");
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("2", getText(bccPanel.getRevisionField()));
        bccPanel.setCardinalityMaxField("50");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"Max must be greater than\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));
    }

    @Test
    public void test_TA_10_7_8_c_deprecated_in_previous_version() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setDeprecated(true);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("2", getText(bccPanel.getRevisionField()));
        assertChecked(bccPanel.getDeprecatedCheckbox());
        assertDisabled(bccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_7_8_c_not_deprecated_in_previous_version() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setDeprecated(false);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("2", getText(bccPanel.getRevisionField()));
        assertNotChecked(bccPanel.getDeprecatedCheckbox());
        assertEnabled(bccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_7_8_d() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("2", getText(bccPanel.getRevisionField()));
        //check max greater than min
        bccPanel.setCardinalityMinField("111");
        bccPanel.setCardinalityMaxField("11");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(),\"must be less than or equal\")]")).size());
        accViewEditPage.hitUpdateButton();
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]"))));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update anyway\")]//ancestor::button[1]")));

        assertEquals("11", getText(bccPanel.getCardinalityMinField()));

    }

    @Test
    public void test_TA_10_7_8_e() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("2", getText(bccPanel.getRevisionField()));
        assertDisabled(bccPanel.getEntityTypeSelectField());

    }

    @Test
    public void test_TA_10_7_8_f() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
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
    public void test_TA_10_7_9() {


    }

    @Test
    public void test_TA_10_7_10() {


    }

    @Test
    public void test_TA_10_7_11() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
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

        accViewEditPage.hitReviseButton();

        appendASCCPDialog = accViewEditPage.insertPropertyAfter("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        appendASCCPDialog.selectAssociation(asccp_after.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.removeAssociation("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        accViewEditPage.removeAssociation("/" + acc.getDen() + "/" + asccp_after.getPropertyTerm());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        assertFalse(asccNode.isDisplayed());
        asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp_after.getPropertyTerm());
        assertFalse(asccNode.isDisplayed());
        viewEditCoreComponentPage.openPage();
        accViewEditPage.hitCancelButton();

        asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        assertTrue(asccNode.isDisplayed());
    }

    @Test
    public void test_TA_10_7_12_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        acc.setBasedAccManifestId(accForBase.getBasedAccManifestId());
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Published");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespace, ccStates);

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            WebElement accBaseNode;
            ACCViewEditPage.ACCPanel accBasePanel;
            ACCSetBaseACCDialog accSetBaseACCDialog;

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
    public void test_TA_10_7_12_b() {
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
        acc.setBasedAccManifestId(accForBase.getBasedAccManifestId());
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;
        ACCSetBaseACCDialog accSetBaseACCDialog;

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
        retry(() -> {
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
                    By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]"))));

        });
    }

    @Test
    public void test_TA_10_7_12_c() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        acc.setBasedAccManifestId(accForBase.getBasedAccManifestId());
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;
        ACCSetBaseACCDialog accSetBaseACCDialog;

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
        accSetBaseACCDialog.hitApplyButton("Any Structured Content. Details");
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Any Structured Content. Details");
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals("Semantics", getText(accBasePanel.getComponentTypeSelectField()));

    }

    @Test
    public void test_TA_10_7_12_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        acc.setBasedAccManifestId(accForBase.getBasedAccManifestId());
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;
        ACCSetBaseACCDialog accSetBaseACCDialog;

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
    public void test_TA_10_7_12_ef() {
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

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
        ACCSetBaseACCDialog accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.setDEN(accForBase.getDen());
        accSetBaseACCDialog.hitSearchButton();
        By APPLY_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Apply\")]//ancestor::button[1]");

        ACCObject finalAccForBase = accForBase;
        String asccpPropertyTerm = asccp.getPropertyTerm();
        String duplicateWarning = "There is a conflict in ASCCPs between the current ACC and the base ACC [" + asccpPropertyTerm + "]";
        retry(() -> {
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
            assertTrue(duplicateWarning.equals(getSnackBarMessage(getDriver())));
        });
    }

    @Test
    public void test_TA_10_7_13() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        acc.setBasedAccManifestId(accForBase.getBasedAccManifestId());
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;
        ACCSetBaseACCDialog accSetBaseACCDialog;

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals(accForBase.getDen(), getText(accBasePanel.getDENField()));
        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + accForBase.getDen());

        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        assertFalse(accBaseNode.isDisplayed());

    }

    @Test
    public void test_TA_10_7_14() {
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

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
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
        accViewEditPage.hitReviseButton();
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

            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
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
    public void test_TA_10_7_15() {
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
            WebElement movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            assertTrue(movedBCCPNode.isDisplayed());
        }

    }

    @Test
    public void test_TA_10_7_16() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_association, accForBaseLv1, accForBaseLv2, accForBaseLv3;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            accForBaseLv1 = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            acc.setBasedAccManifestId(accForBaseLv1.getAccManifestId());
            coreComponentAPI.updateACC(acc);

            accForBaseLv2 = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            accForBaseLv1.setBasedAccManifestId(accForBaseLv2.getAccManifestId());
            coreComponentAPI.updateACC(accForBaseLv1);

            accForBaseLv3 = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            accForBaseLv2.setBasedAccManifestId(accForBaseLv3.getAccManifestId());
            coreComponentAPI.updateACC(accForBaseLv2);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(accForBaseLv1, bccp, "WIP");
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
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        viewEditCoreComponentPage.openPage();
        {
            String nodePath = "/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, bccp_to_append.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordByValue(accForBaseLv2.getObjectClassTerm());
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedBCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBaseLv2.getDen() + "/" + bccp_to_append.getPropertyTerm());
            assertTrue(movedBCCPNode.isDisplayed());

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            movedBCCPNode = accViewEditPage.getNodeByPath(nodePath);
            assertFalse(movedBCCPNode.isDisplayed());
        }

        {
            String nodePath = "/" + acc.getDen() + "/" + asccp.getPropertyTerm();
            SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = accViewEditPage.refactorToBaseACC(nodePath, asccp.getPropertyTerm());
            WebElement tr;
            tr = selectBaseACCToRefactorDialog.getTableRecordByValue(accForBaseLv3.getObjectClassTerm());
            assertTrue(tr.isDisplayed());
            click(tr.findElement(By.className("mat-column-" + "select")));
            selectBaseACCToRefactorDialog.hitAnalyzeButton();
            assertEnabled(selectBaseACCToRefactorDialog.getRefactorButton(true));
            selectBaseACCToRefactorDialog.hitRefactorButton();

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement movedASCCPNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBaseLv3.getDen() + "/" + asccp.getPropertyTerm());
            assertTrue(movedASCCPNode.isDisplayed());

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            movedASCCPNode = accViewEditPage.getNodeByPath(nodePath);
            assertFalse(movedASCCPNode.isDisplayed());
        }

    }

}
