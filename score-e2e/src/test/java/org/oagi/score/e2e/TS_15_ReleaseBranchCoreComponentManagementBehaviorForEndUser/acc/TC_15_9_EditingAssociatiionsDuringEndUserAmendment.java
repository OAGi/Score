package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.acc;

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

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_15_9_EditingAssociatiionsDuringEndUserAmendment extends BaseTest {
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
    public void selected_asccp_can_be_in_any_state() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "Production");
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Production");
        ccStates.add("QA");
        ccStates.add("WIP");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(endUser, release, namespace, ccStates);

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

        ccStates = new ArrayList<>();
        ccStates.add("Published");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        NamespaceObject namespaceForDeveloper = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespaceForDeveloper, ccStates);


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
    public void warning_shall_be_given_if_the_asccp_is_deprecated() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "Production");
        ACCObject acc_association = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "Production");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, endUser, namespace, "Production");
        viewEditCoreComponentPage.openPage();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitAmendButton();
        asccpViewEditPage.toggleDeprecated();
        asccpViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
                By.xpath("//score-confirm-dialog//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));
    }

    @Test
    public void asccp_shall_not_violate_the_property_uniqueness_constraint_of_the_acc_in_other_words() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation("Account Identifiers. Named Identifiers");
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation("Account Identifiers. Named Identifiers");
        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//score-multi-actions-snack-bar//div[contains(@class, \"header\")]")).isDisplayed();

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("already has ASCCP"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//score-multi-actions-snack-bar//span[contains(text(), \"Close\")]//ancestor::button[1]")));

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Account Identifiers");
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();

        assertEquals("0", getText(asccPanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(asccPanel.getCardinalityMaxField()));
        assertNotChecked(asccPanel.getDeprecatedCheckbox());
        assertDisabled(asccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void resulting_ascc_shall_be_in_the_wip_state() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "Production");
        ACCObject acc_association = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "Production");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, endUser, namespace, "Production");

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("WIP", getText(asccPanel.getStateField()));
    }

    @Test
    public void if_the_asccp_is_not_reusable_check_that_there_is_no_ascc_already_using() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
        ACCObject acc_WithNonReusableAsccp = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
        ACCObject acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
        ASCCPObject asccp_NotReusable = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "WIP");
        ASCCObject ascc = coreComponentAPI.appendASCC(acc_WithNonReusableAsccp, asccp_NotReusable, "WIP");
        ascc.setCardinalityMax(1);
        coreComponentAPI.updateASCC(ascc);

        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp_NotReusable.getAsccpManifestId());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
        asccpPanel.toggleReusable();
        asccpViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp_NotReusable.getDen());

        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//score-multi-actions-snack-bar//div[contains(@class, \"header\")]")).isDisplayed();

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.equals("Target ASCCP is not reusable and already has 1 ASCC reference(s)."));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//score-multi-actions-snack-bar//span[contains(text(), \"Close\")]//ancestor::button[1]")));
    }

    @Test
    public void end_user_can_right_click_on_any_associations_and_insert_an_asccp_before_or() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        ACCObject acc_association = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        ASCCPObject asccp, asccp_before, asccp_after;
        asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, anotherUser, namespace, "WIP");
        asccp_after = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, anotherUser, namespace, "WIP");
        asccp_before = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc_association, anotherUser, namespace, "WIP");

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
    public void min_0() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        ACCObject acc, acc_association;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
    public void max_1_and_max_min_when_max_1() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
        asccPanel.setCardinalityMaxField("222");
        click(accViewEditPage.getUpdateButton(true));
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update anyway\")]//ancestor::button[1]")));

        assertEquals("111", getText(asccPanel.getCardinalityMinField()));
    }

    @Test
    public void user_may_type_in_unbounded_in_place_of_1_for_max_if_the_user() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
    public void min_max_and_deprecated_are_required() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
    public void warning_should_be_given_when_the_definition_is_empty() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        asccPanel.setCardinalityMinField("11");
        asccPanel.setCardinalityMaxField("111");
        click(accViewEditPage.getUpdateButton(true));
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));
    }

    @Test
    public void deprecated_must_be_false_and_locked_because_it_is_a_new_association_it_shouldn() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "WIP");

        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendASCCPDialog.selectAssociation(asccp.getDen());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertNotChecked(asccPanel.getDeprecatedCheckbox());
        assertDisabled(asccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void selected_bccp_can_be_in_any_state() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "Production");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Published");
        ccStates.add("Deleted");
        NamespaceObject namespaceForDeveloper = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespaceForDeveloper, ccStates);

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

        ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");
        ccStates.add("Deleted");
        randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(endUser, release, namespace, ccStates);

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
    public void warning_shall_be_given_if_the_bccp_is_deprecated() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp_to_append.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        bccpPanel.toggleDeprecated();
        bccpViewEditPage.hitUpdateButton();

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
                By.xpath("//score-confirm-dialog//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));
    }

    @Test
    public void added_bccp_shall_not_cause_a_property_uniqueness_violation_to_the_acc() {
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation("Accrued Amount");

        appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation("Accrued Amount");
        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//score-multi-actions-snack-bar//div[contains(@class, \"header\")]")).isDisplayed();

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("already has BCCP"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//score-multi-actions-snack-bar//span[contains(text(), \"Close\")]//ancestor::button[1]")));

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/Accrued Amount");
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(bccPanel.getCardinalityMaxField()));
        assertNotChecked(bccPanel.getDeprecatedCheckbox());
        assertDisabled(bccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void resulting_bcc_shall_be_in_the_wip_state() {
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());

        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("WIP", getText(bccPanel.getStateField()));
    }

    @Test
    public void if_the_entity_type_is_changed_from_element_to_attribute_the_min_cardinality_should() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("Element", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("None", getText(bccPanel.getValueConstraintSelectField()));
        assertDisabled(bccPanel.getValueConstraintSelectField());
        bccPanel.setEntityType("Attribute");
        assertEnabled(bccPanel.getValueConstraintSelectField());
        bccPanel.setDefinition("test");
        accViewEditPage.hitUpdateButton();

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
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
    public void entity_type_can_be_changed_to_attribute_only_when_the_bccp_has_no_sc() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
    public void end_user_can_right_click_on_any_associations_and_insert_a_bccp_before_or() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject enduserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        BCCPObject bccp_endUser;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp_endUser = coreComponentAPI.createRandomBCCP(release, dataType, endUser, enduserNamespace, "Production");
        }
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, enduserNamespace, "Production");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        BCCPObject bccp, bccp_before, bccp_after;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, enduserNamespace, "Production");
            bccp_before = coreComponentAPI.createRandomBCCP(release, dataType, endUser, enduserNamespace, "Production");
            bccp_after = coreComponentAPI.createRandomBCCP(release, dataType, endUser, enduserNamespace, "Production");
        }

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        SelectAssociationDialog appendBCCPDialog;
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
    public void min_0_scenario_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));

        bccPanel.setCardinalityMinField("-1");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"is not allowed for Cardinality Min\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));

        bccPanel.setCardinalityMinField("10");
        bccPanel.setDefinition("Test cardinality min must be positive");
        accViewEditPage.hitUpdateButton();
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("10", getText(bccPanel.getCardinalityMinField()));
    }

    @Test
    public void max_1_and_max_min_when_max_1_scenario_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertEquals("0", getText(bccPanel.getCardinalityMinField()));

        bccPanel.setCardinalityMinField("-9");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"is not allowed for Cardinality Min\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));

        bccPanel.setCardinalityMaxField("11");
        bccPanel.setCardinalityMinField("111");
        bccPanel.setDefinition("Test cardinality min must be less than or equal to cardinality max");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"Cardinality Min must be less than or equals to\")]")).size());
        bccPanel.setCardinalityMinField("5");
        accViewEditPage.hitUpdateButton();
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("11", getText(bccPanel.getCardinalityMaxField()));
    }

    @Test
    public void user_may_type_in_unbounded_in_place_of_1_for_max_if_the_user_scenario_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
    public void all_fields_except_definition_and_definition_source_are_required_however_a_warning_should_be() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
                By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));
    }

    @Test
    public void deprecated_must_be_false_because_it_is_a_new_association_it_shouldn_t_be() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();

        assertNotChecked(bccPanel.getDeprecatedCheckbox());
        assertDisabled(bccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void default_and_fixed_value_shall_be_disabled_and_cleared_of_value_if_the_entity() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc;
        BCCPObject bccp, bccp_to_append;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendBCCPDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
        appendBCCPDialog.selectAssociation(bccp_to_append.getDen());

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
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

        bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp_to_append.getPropertyTerm());
        bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
        assertEquals("Attribute", getText(bccPanel.getEntityTypeSelectField()));
        assertEquals("Default Value", getText(bccPanel.getValueConstraintSelectField()));
        assertEquals("99", getText(bccPanel.getDefaultValueField()));
    }

    @Test
    public void end_user_can_change_move_up_or_down_position_of_the_brand_new_association() {

    }

    @Test
    public void end_user_can_change_move_up_or_down_position_of_the_brand_new_association_scenario_2() {

    }

    @Test
    public void end_user_can_remove_a_brand_new_association_ascc_or_bcc_if_the_association() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc, acc_association, acc_association_before, acc_association_after;
        ASCCPObject asccp, asccp_before, asccp_after;
        ASCCObject ascc, ascc_before;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            acc_association_before = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            acc_association_after = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "Production");
            asccp_after = coreComponentAPI.createRandomASCCP(acc_association_after, endUser, namespace, "Production");
            asccp_before = coreComponentAPI.createRandomASCCP(acc_association_before, endUser, namespace, "Production");
            ascc = getAPIFactory().getCoreComponentAPI().appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
            ascc_before = coreComponentAPI.appendASCC(acc, asccp_before, "Production");
            ascc_before.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc_before);
        }

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        SelectAssociationDialog appendASCCPDialog = accViewEditPage.insertPropertyAfter("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        appendASCCPDialog.selectAssociation(asccp_after.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.removeAssociation("/" + acc.getDen() + "/" + asccp_after.getPropertyTerm());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        String xpathExpr = "//cdk-virtual-scroll-viewport//div//span[contains(@class, \"search-index\")]//*[contains(text(),\"" + asccp_after.getPropertyTerm() + "\")]";
        assertEquals(0, getDriver().findElements(By.xpath(xpathExpr)).size());
    }

    @Test
    public void new_based_acc_can_be_in_any_state() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        getAPIFactory().getCoreComponentAPI().updateBasedACC(acc, accForBase);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(anotherUser, release, namespace, ccStates);

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
    public void if_the_chosen_based_acc_is_deprecated_a_warning_shall_be_given() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "Production");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "Production");
        getAPIFactory().getCoreComponentAPI().updateBasedACC(acc, accForBase);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;
        ACCSetBaseACCDialog accSetBaseACCDialog;
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals(accForBase.getDen(), getText(accBasePanel.getDENField()));
        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + accForBase.getDen());

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "WIP");
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
                By.xpath("//score-confirm-dialog//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));
    }

    @Test
    public void based_acc_can_only_be_base_or_semantics_component_type() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        getAPIFactory().getCoreComponentAPI().updateBasedACC(acc, accForBase);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
        accSetBaseACCDialog.hitApplyButton(accForBase.getDen());
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals("Semantics", getText(accBasePanel.getComponentTypeSelectField()));
    }

    @Test
    public void end_user_cannot_change_the_fields_of_the_base_acc() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        getAPIFactory().getCoreComponentAPI().updateBasedACC(acc, accForBase);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
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
    public void based_acc_should_not_contain_an_asccp_or_a_bccp_with_the_same_property() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        ACCObject acc, accForBase, acc_association;
        ASCCObject ascc, asccForBase;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            accForBase = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
            asccForBase = coreComponentAPI.appendASCC(accForBase, asccp, "Production");
            asccForBase.setCardinalityMax(1);
            coreComponentAPI.updateASCC(asccForBase);
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        ACCSetBaseACCDialog accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.setDEN(accForBase.getDen());
        accSetBaseACCDialog.hitSearchButton();
        By APPLY_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Apply\")]//ancestor::button[1]");

        ACCObject finalAccForBase = accForBase;
        String asccpPropertyTerm = asccp.getPropertyTerm();
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

        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//score-multi-actions-snack-bar//div[contains(@class, \"header\")]")).isDisplayed();

        String xpathExpr = "//score-multi-actions-snack-bar//div[contains(@class, \"message\")]";
        String snackBarMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertTrue(snackBarMessage.contains("There is a conflict in ASCCPs between the current ACC and the base ACC"));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//score-multi-actions-snack-bar//span[contains(text(), \"Close\")]//ancestor::button[1]")));
    }

    @Test
    public void end_user_can_remove_the_based_acc_when_one_already_exists_before_the_amendment() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject accForBase = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "Production");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "Production");
        getAPIFactory().getCoreComponentAPI().updateBasedACC(acc, accForBase);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        WebElement accBaseNode;
        ACCViewEditPage.ACCPanel accBasePanel;
        ACCSetBaseACCDialog accSetBaseACCDialog;

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accBaseNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen());
        accBasePanel = accViewEditPage.getACCPanel(accBaseNode);
        assertEquals(accForBase.getDen(), getText(accBasePanel.getDENField()));
        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + accForBase.getDen());

        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        String xpathExpr = "//cdk-virtual-scroll-viewport//div//span[contains(@class, \"search-index\")]//*[contains(text(),\"" + accForBase.getDen() + "\")]";
        assertEquals(0, getDriver().findElements(By.xpath(xpathExpr)).size());
    }

    @Test
    public void end_user_can_transfer_the_ownership_of_an_acc_which_is_in_wip_states() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        viewEditCoreComponentPage.openPage();
        {
            viewEditCoreComponentPage.setBranch(branch);
            waitFor(Duration.ofMillis(1500));
            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();

            WebElement tr = viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
            WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
            assertTrue(td.findElement(By.className("mat-icon")).isEnabled());
            TransferCCOwnershipDialog transferCCOwnershipDialog =
                    viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
            transferCCOwnershipDialog.transfer(anotherUser.getLoginId());

            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();

            tr = viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
            td = viewEditCoreComponentPage.getColumnByName(tr, "owner");
            assertEquals(anotherUser.getLoginId(), getText(td));

            //verify the ownership of all associations (ASCC and BCC) are  transferred as well
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
            assertEquals(anotherUser.getLoginId(), getText(asccPanel.getOwnerField()));

            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
            assertEquals(anotherUser.getLoginId(), getText(bccPanel.getOwnerField()));
        }

        homePage.logout();
        homePage = loginPage().signIn(anotherUser.getLoginId(), anotherUser.getPassword());
        viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        {
            viewEditCoreComponentPage.setBranch(branch);
            waitFor(Duration.ofMillis(1500));
            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();

            WebElement tr = viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
            WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
            assertTrue(td.findElement(By.className("mat-icon")).isEnabled());

            TransferCCOwnershipDialog transferCCOwnershipDialog =
                    viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
            assertThrows(NoSuchElementException.class, () -> transferCCOwnershipDialog.transfer(developer.getLoginId()));
        }
    }

    @Test
    public void end_user_cannot_remove_assc_or_bcc_existed_before_the_amendment() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement asccpNode = accViewEditPage.clickOnDropDownMenuByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        By REMOVE_OPTION_LOCATOR =
                By.xpath("//span[contains(text(), \"Remove\")]");
        assertThrows(TimeoutException.class, () -> visibilityOfElementLocated(getDriver(), REMOVE_OPTION_LOCATOR));
        escape(getDriver());

        WebElement bccpNode = accViewEditPage.clickOnDropDownMenuByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        assertThrows(TimeoutException.class, () -> visibilityOfElementLocated(getDriver(), REMOVE_OPTION_LOCATOR));
        escape(getDriver());
    }

    @Test
    public void scenario_0_min_previousmin() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMin(25);
            coreComponentAPI.updateASCC(ascc);
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("2", getText(asccPanel.getRevisionField()));
        asccPanel.setCardinalityMinField("30");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"Min must be less than or equals to\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));
    }

    @Test
    public void if_previous_max_1_it_cannot_be_changed_otherwise_max_1_or_max_previous() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(75);
            coreComponentAPI.updateASCC(ascc);

        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("2", getText(asccPanel.getRevisionField()));
        asccPanel.setCardinalityMaxField("50");
        assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(), \"Max must be greater than\")]")).size());
        assertDisabled(accViewEditPage.getUpdateButton(false));
    }

    @Test
    public void if_the_deprecated_was_already_true_before_the_amendment_the_field_along_with_the() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setDeprecated(true);
            coreComponentAPI.updateASCC(ascc);

        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("2", getText(asccPanel.getRevisionField()));
        assertChecked(asccPanel.getDeprecatedCheckbox());
        assertEnabled(asccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void if_the_deprecated_was_already_true_before_the_amendment_the_field_along_with_the_scenario_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setDeprecated(false);
            coreComponentAPI.updateASCC(ascc);

        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("2", getText(asccPanel.getRevisionField()));
        assertNotChecked(asccPanel.getDeprecatedCheckbox());
        assertEnabled(asccPanel.getDeprecatedCheckbox());
    }

    @Test
    public void warning_should_be_given_when_the_definition_is_empty_scenario_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            coreComponentAPI.updateASCC(ascc);

        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
        assertEquals("2", getText(asccPanel.getRevisionField()));
        //check max greater than min
        asccPanel.setDefinition(null);
        click(accViewEditPage.getUpdateButton(true));
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update anyway\")]//ancestor::button[1]")));
    }

    @Test
    public void end_user_can_cancel_the_amendment_in_such_case_all_changes_to_the_acc() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        ACCObject acc, acc_association, accForBase;
        ASCCObject ascc;
        ASCCPObject asccp;
        BCCPObject bccp;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            accForBase = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            coreComponentAPI.updateBasedACC(acc, accForBase);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            bcc.setDefinition("oldDef");
            bcc.setDefinitionSource("aDefSource");
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace, "Production");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            ascc.setDefinition("oldDef");
            ascc.setDefinitionSource("aDefSource");
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

        String nodePath;

        {
            nodePath = "/" + acc.getDen() + "/" + asccp.getPropertyTerm();
            accViewEditPage.openPage();
            WebElement asccNode = accViewEditPage.getNodeByPath(nodePath);
            ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
            asccPanel.setCardinalityMaxField("50");
            asccPanel.setDefinition("changeDefinition");
            accViewEditPage.hitUpdateButton();
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            asccNode = accViewEditPage.getNodeByPath(nodePath);
            asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
            assertEquals("50", getText(asccPanel.getCardinalityMaxField()));
            assertEquals("changeDefinition", getText(asccPanel.getDefinitionField()));
        }

        {
            nodePath = "/" + acc.getDen() + "/" + bccp.getPropertyTerm();
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement bccNode = accViewEditPage.getNodeByPath(nodePath);
            ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
            bccPanel.setCardinalityMaxField("70");
            bccPanel.setDefinition("changeDefinition");
            accViewEditPage.hitUpdateButton();
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            bccNode = accViewEditPage.getNodeByPath(nodePath);
            bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
            assertEquals("70", getText(bccPanel.getCardinalityMaxField()));
            assertEquals("changeDefinition", getText(bccPanel.getDefinitionField()));
        }

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitCancelButton();

        {
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
            assertEquals("1", getText(asccPanel.getCardinalityMaxField()));
            assertEquals("oldDef", getText(asccPanel.getDefinitionField()));
        }

        {
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
            ACCViewEditPage.BCCPanel bccPanel = accViewEditPage.getBCCPanelContainer(bccNode).getBCCPanel();
            assertEquals("1", getText(bccPanel.getCardinalityMaxField()));
            assertEquals("oldDef", getText(bccPanel.getDefinitionField()));
        }
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

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryById(release.getLibraryId());

            for (int i = 0; i < this.states.size(); ++i) {
                ASCCPObject asccp;
                BCCPObject bccp;
                ACCObject acc;
                String state = this.states.get(i);

                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

                    acc = coreComponentAPI.createRandomACC(this.appUser, release, namespace, state);
                    DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
                    bccp = coreComponentAPI.createRandomBCCP(release, dataType, this.appUser, namespace, state);
                    BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, state);
                    bcc.setCardinalityMax(1);
                    coreComponentAPI.updateBCC(bcc);

                    ACCObject acc_association = coreComponentAPI.createRandomACC(this.appUser, release, namespace, state);
                    BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, this.appUser, namespace, state);
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
}
