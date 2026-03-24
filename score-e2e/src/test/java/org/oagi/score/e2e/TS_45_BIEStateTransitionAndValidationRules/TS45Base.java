package org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@ResourceLock("TS_45_BIE_STATE_TRANSITION")
public abstract class TS45Base extends BaseTest {

    protected static final String BIE_MUST_BE_IN_QA_MESSAGE = "This BIE must be in 'QA'.";
    protected static final String BIE_MUST_BE_IN_PRODUCTION_MESSAGE = "This BIE must be in 'Production'.";
    protected static final String BIE_MUST_BE_IN_WIP_MESSAGE = "This BIE must be in 'WIP'.";
    protected static final String BIE_MUST_BE_DISCARDED_MESSAGE = "This BIE must be discarded.";
    protected static final String CODE_LIST_MUST_BE_IN_QA_MESSAGE = "This code list must be in 'QA'.";
    protected static final String CODE_LIST_MUST_BE_IN_PRODUCTION_MESSAGE = "This code list must be in 'Production'.";
    protected static final String VALIDATION_SUMMARY_QA = "This BIE cannot move to 'QA'.";
    protected static final String VALIDATION_SUMMARY_PRODUCTION = "This BIE cannot move to 'Production'.";
    protected static final String VALIDATION_SUMMARY_WIP = "This BIE cannot move to 'WIP'.";
    protected static final String VALIDATION_SUMMARY_DISCARD = "This BIE cannot be discarded.";
    protected static final String VALIDATION_SUMMARY_MULTI_DISCARD = "Selected BIEs cannot be discarded.";

    protected final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        this.randomAccounts.forEach(newUser ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId()));
    }

    protected void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    protected TestGraph createTestGraph() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        return createConfiguredTestGraph(
                endUser,
                null,
                endUser,
                endUser,
                endUser,
                endUser,
                "WIP",
                "WIP");
    }

    protected TestGraph createTestGraphWithCrossOwnerHeaderDependency() {
        AppUserObject transitionOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject otherOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(transitionOwner);
        thisAccountWillBeDeletedAfterTests(otherOwner);

        return createConfiguredTestGraph(
                transitionOwner,
                otherOwner,
                otherOwner,
                transitionOwner,
                transitionOwner,
                otherOwner,
                "WIP",
                "QA");
    }

    protected TestGraph createTestGraphWithCrossOwnerPrimaryCodeList() {
        AppUserObject transitionOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject otherOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(transitionOwner);
        thisAccountWillBeDeletedAfterTests(otherOwner);

        return createConfiguredTestGraph(
                transitionOwner,
                otherOwner,
                transitionOwner,
                transitionOwner,
                otherOwner,
                transitionOwner,
                "WIP",
                "WIP");
    }

    protected TestGraph createMinimalBaseDiscardTestGraph() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.11");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .createRandomEndUserNamespace(endUser, library);

        ASCCPObject bomHeaderAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Header. BOM Header", release.getReleaseNumber());
        ASCCPObject bomAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", release.getReleaseNumber());
        ASCCPObject securityClassificationAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(
                        library,
                        "Security Classification. Security Classification",
                        release.getReleaseNumber());

        TopLevelASBIEPObject sharedHeaderBaseBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(
                        Arrays.asList(businessContext),
                        bomHeaderAsccp,
                        endUser,
                        "WIP");
        TopLevelASBIEPObject primaryBaseBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(
                        Arrays.asList(businessContext),
                        bomAsccp,
                        endUser,
                        "WIP");
        TopLevelASBIEPObject sharedReusableClassificationBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(
                        Arrays.asList(businessContext),
                        securityClassificationAsccp,
                        endUser,
                        "WIP");

        CodeListObject primaryAssignedCodeList = getAPIFactory().getCodeListAPI()
                .createRandomCodeList(endUser, namespace, release, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(primaryBaseBIE);
        WebElement rootTypeCodeNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/Type Code");
        EditBIEPage.BBIEPanel rootTypeCodePanel = editBIEPage.getBBIEPanel(rootTypeCodeNode);
        rootTypeCodePanel.toggleUsed();
        rootTypeCodePanel.setValueDomainRestriction("Code");
        rootTypeCodePanel.setValueDomain(primaryAssignedCodeList.getName());

        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog =
                editBIEPage.reuseBIEOnNode("/" + bomAsccp.getPropertyTerm() + "/BOM Header");
        selectProfileBIEToReuseDialog.selectBIEToReuse(sharedHeaderBaseBIE);

        editBIEPage.openPage();
        selectProfileBIEToReuseDialog =
                editBIEPage.reuseBIEOnNode("/" + bomAsccp.getPropertyTerm() + "/BOM Item Data/Security Classification");
        selectProfileBIEToReuseDialog.selectBIEToReuse(sharedReusableClassificationBIE);
        editBIEPage.openPage();

        return new TestGraph(
                endUser,
                null,
                businessContext,
                release,
                namespace,
                library,
                bomAsccp,
                bomHeaderAsccp,
                securityClassificationAsccp,
                primaryBaseBIE,
                null,
                sharedHeaderBaseBIE,
                null,
                sharedReusableClassificationBIE,
                primaryAssignedCodeList,
                null,
                homePage);
    }

    private TestGraph createConfiguredTestGraph(AppUserObject transitionOwner,
                                                AppUserObject otherOwner,
                                                AppUserObject sharedHeaderBaseOwner,
                                                AppUserObject reusableClassificationOwner,
                                                AppUserObject primaryCodeListOwner,
                                                AppUserObject secondaryCodeListOwner,
                                                String primaryCodeListState,
                                                String secondaryCodeListState) {
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(transitionOwner);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.11");
        NamespaceObject transitionNamespace = getAPIFactory().getNamespaceAPI()
                .createRandomEndUserNamespace(transitionOwner, library);
        NamespaceObject otherNamespace = (otherOwner != null)
                ? getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(otherOwner, library)
                : transitionNamespace;

        ASCCPObject bomHeaderAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Header. BOM Header", release.getReleaseNumber());
        ASCCPObject bomAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", release.getReleaseNumber());
        ASCCPObject securityClassificationAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(
                        library,
                        "Security Classification. Security Classification",
                        release.getReleaseNumber());

        TopLevelASBIEPObject sharedHeaderBaseBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(
                        Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(library, bomHeaderAsccp.getDen(), release.getReleaseNumber()),
                        sharedHeaderBaseOwner,
                        "WIP");
        TopLevelASBIEPObject primaryBaseBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(
                        Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(library, bomAsccp.getDen(), release.getReleaseNumber()),
                        transitionOwner,
                        "WIP");
        TopLevelASBIEPObject sharedReusableClassificationBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(
                        Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(
                                        library,
                                        securityClassificationAsccp.getDen(),
                                        release.getReleaseNumber()),
                        reusableClassificationOwner,
                        "WIP");

        CodeListObject primaryAssignedCodeList = getAPIFactory().getCodeListAPI()
                .createRandomCodeList(
                        primaryCodeListOwner,
                        namespaceFor(primaryCodeListOwner, transitionOwner, transitionNamespace, otherNamespace),
                        release,
                        primaryCodeListState);
        CodeListObject secondaryAssignedCodeList = getAPIFactory().getCodeListAPI()
                .createRandomCodeList(
                        secondaryCodeListOwner,
                        namespaceFor(secondaryCodeListOwner, transitionOwner, transitionNamespace, otherNamespace),
                        release,
                        secondaryCodeListState);

        HomePage homePage = loginPage().signIn(sharedHeaderBaseOwner.getLoginId(), sharedHeaderBaseOwner.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(sharedHeaderBaseBIE);
        WebElement typeCodeNode = editBIEPage.getNodeByPath(
                "/" + bomHeaderAsccp.getPropertyTerm() + "/Identifier/Type Code", 3);
        EditBIEPage.BBIEPanel typeCodePanel = editBIEPage.getBBIEPanel(typeCodeNode);
        typeCodePanel.toggleUsed();
        typeCodePanel.setValueDomainRestriction("Code");
        typeCodePanel.setValueDomain(secondaryAssignedCodeList.getName());
        editBIEPage.hitUpdateButton();

        if (!sharedHeaderBaseOwner.getAppUserId().equals(transitionOwner.getAppUserId())) {
            homePage.logout();
            homePage = loginPage().signIn(transitionOwner.getLoginId(), transitionOwner.getPassword());
        }

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(primaryBaseBIE);
        WebElement rootTypeCodeNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/Type Code");
        EditBIEPage.BBIEPanel rootTypeCodePanel = editBIEPage.getBBIEPanel(rootTypeCodeNode);
        rootTypeCodePanel.toggleUsed();
        rootTypeCodePanel.setValueDomainRestriction("Code");
        rootTypeCodePanel.setValueDomain(primaryAssignedCodeList.getName());

        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog =
                editBIEPage.reuseBIEOnNode("/" + bomAsccp.getPropertyTerm() + "/BOM Header");
        selectProfileBIEToReuseDialog.selectBIEToReuse(sharedHeaderBaseBIE);

        editBIEPage.openPage();
        selectProfileBIEToReuseDialog =
                editBIEPage.reuseBIEOnNode("/" + bomAsccp.getPropertyTerm() + "/BOM Item Data/Security Classification");
        selectProfileBIEToReuseDialog.selectBIEToReuse(sharedReusableClassificationBIE);
        editBIEPage.openPage();

        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        EditBIEPage sharedHeaderDerivedEditPage =
                createInheritedBIE(viewEditBIEPage, businessContext, sharedHeaderBaseOwner, transitionOwner, bomHeaderAsccp);
        TopLevelASBIEPObject sharedHeaderDerivedBIE = sharedHeaderDerivedEditPage.getTopLevelASBIEP();
        String sharedHeaderDerivedVersion = "version_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String sharedHeaderDerivedStatus = "status_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        sharedHeaderDerivedBIE.setVersion(sharedHeaderDerivedVersion);
        sharedHeaderDerivedBIE.setStatus(sharedHeaderDerivedStatus);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = sharedHeaderDerivedEditPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setVersion(sharedHeaderDerivedVersion);
        topLevelASBIEPPanel.setStatus(sharedHeaderDerivedStatus);
        sharedHeaderDerivedEditPage.hitUpdateButton();

        EditBIEPage primaryDerivedEditPage =
                createInheritedBIE(viewEditBIEPage, businessContext, transitionOwner, transitionOwner, bomAsccp);
        TopLevelASBIEPObject primaryDerivedBIE = primaryDerivedEditPage.getTopLevelASBIEP();
        String primaryDerivedVersion = "version_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String primaryDerivedStatus = "status_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        primaryDerivedBIE.setVersion(primaryDerivedVersion);
        primaryDerivedBIE.setStatus(primaryDerivedStatus);
        topLevelASBIEPPanel = primaryDerivedEditPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setVersion(primaryDerivedVersion);
        topLevelASBIEPPanel.setStatus(primaryDerivedStatus);
        primaryDerivedEditPage.hitUpdateButton();

        selectProfileBIEToReuseDialog =
                primaryDerivedEditPage.openOverrideBaseReusedBIEDialog("/" + bomAsccp.getPropertyTerm() + "/BOM Header");
        selectProfileBIEToReuseDialog.selectBIEToReuse(sharedHeaderDerivedBIE);
        primaryDerivedEditPage.openPage();

        return new TestGraph(
                transitionOwner,
                otherOwner,
                businessContext,
                release,
                transitionNamespace,
                library,
                bomAsccp,
                bomHeaderAsccp,
                securityClassificationAsccp,
                primaryBaseBIE,
                primaryDerivedBIE,
                sharedHeaderBaseBIE,
                sharedHeaderDerivedBIE,
                sharedReusableClassificationBIE,
                primaryAssignedCodeList,
                secondaryAssignedCodeList,
                homePage);
    }

    private NamespaceObject namespaceFor(AppUserObject owner,
                                         AppUserObject transitionOwner,
                                         NamespaceObject transitionNamespace,
                                         NamespaceObject otherNamespace) {
        if (owner.getAppUserId().equals(transitionOwner.getAppUserId())) {
            return transitionNamespace;
        }
        return otherNamespace;
    }

    protected EditBIEPage openEditBIEPage(HomePage homePage, TopLevelASBIEPObject topLevelASBIEP) {
        return homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
    }

    protected void setBIEState(TopLevelASBIEPObject topLevelASBIEP, String state) {
        topLevelASBIEP.setState(state);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(topLevelASBIEP);
    }

    protected void setCodeListState(CodeListObject codeList, String state) {
        codeList.setState(state);
        getAPIFactory().getCodeListAPI().updateCodeList(codeList);
    }

    protected void assertBIEState(TopLevelASBIEPObject topLevelASBIEP, String state) {
        assertEquals(state, getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelASBIEP.getTopLevelAsbiepId()).getState());
    }

    protected void assertCodeListState(CodeListObject codeList, String state) {
        assertEquals(state, getAPIFactory().getCodeListAPI()
                .getCodeListByManifestId(codeList.getCodeListManifestId()).getState());
    }

    protected void openMoveToQADialog(EditBIEPage editBIEPage) {
        click(editBIEPage.getMoveToQAButton(true));
    }

    protected void openMoveToProductionDialog(EditBIEPage editBIEPage) {
        click(editBIEPage.getMoveToProductionButton(true));
    }

    protected void openBackToWIPDialog(EditBIEPage editBIEPage) {
        click(editBIEPage.getBackToWIPButton(true));
    }

    protected void openDiscardDialog(EditBIEPage editBIEPage) {
        click(elementToBeClickable(getDriver(), By.xpath(
                "//button[.//span[normalize-space(.) = \"Discard\"] and not(ancestor::mat-dialog-container)]")));
    }

    protected void confirmDependencyDialogUpdate() {
        click(getDependencyDialogUpdateButton());
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
        assertEquals("State updated", getSnackBarMessage(getDriver()));
    }

    protected void confirmDependencyDialogDiscard() {
        click(getDependencyDialogDiscardButton());
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
        assertEquals("Discarded", getSnackBarMessage(getDriver()));
    }

    protected void cancelDependencyDialog() {
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Cancel\")]//ancestor::button[1]")));
        waitFor(ofMillis(500L));
    }

    protected void assertDependencySummaryContains(String text) {
        assertTrue(getText(getDependencyValidationSummary()).contains(text));
    }

    protected void assertDependencyRowDisplayed(String value) {
        assertNotNull(getDependencyRowByValue(value));
    }

    protected void assertDependencyRowNotDisplayed(String value) {
        assertTrue(getDriver().findElements(By.xpath(
                "//mat-dialog-container//td//*[contains(text(), \"" + value + "\")]/ancestor::tr")).isEmpty());
    }

    protected void assertDependencyRowMessage(String value, String message) {
        WebElement row = getDependencyRowByValue(value);
        assertTrue(!row.findElements(By.xpath(
                ".//div[contains(@class, 'validation-message') and normalize-space(.)=\"" + message + "\"]")).isEmpty());
    }

    protected String bieOwnershipMessage(AppUserObject owner) {
        return "This BIE is owned by '" + owner.getLoginId() + "' and cannot be updated.";
    }

    protected String bieDiscardOwnershipMessage(AppUserObject owner) {
        return "This BIE is owned by '" + owner.getLoginId() + "' and cannot be discarded.";
    }

    protected String codeListOwnershipMessage(AppUserObject owner) {
        return "This code list is owned by '" + owner.getLoginId() + "' and cannot be updated.";
    }

    protected void assertRemainingMessagesAfterSelection(List<String> bieRowValues, List<String> codeListRowValues,
                                                         String bieMessage, String codeListMessage) {
        for (String bieRowValue : bieRowValues) {
            assertDependencyRowMessage(bieRowValue, bieMessage);
        }
        for (String codeListRowValue : codeListRowValues) {
            assertDependencyRowMessage(codeListRowValue, codeListMessage);
        }
    }

    protected void selectDependencyRow(String value) {
        WebElement row = getDependencyRowByValue(value);
        click(row.findElement(By.xpath("./td[1]//mat-checkbox")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    protected void assertDependencyRowNotSelectable(String value) {
        WebElement row = getDependencyRowByValue(value);
        WebElement checkbox = row.findElement(By.xpath("./td[1]//mat-checkbox"));
        String classes = checkbox.getAttribute("class");
        assertTrue(classes != null && classes.contains("mat-mdc-checkbox-disabled"));
    }

    protected WebElement getDependencyDialogUpdateButton() {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]"));
    }

    protected WebElement getDependencyDialogDiscardButton() {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]"));
    }

    protected WebElement getDependencyValidationSummary() {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-dialog-container//*[contains(@class, \"validation-summary\")]"));
    }

    protected boolean hasValidationSummary() {
        return !getDriver().findElements(By.xpath(
                "//mat-dialog-container//*[contains(@class, \"validation-summary\")]")).isEmpty();
    }

    protected boolean hasDependencyTable() {
        return !getDriver().findElements(By.xpath(
                "//mat-dialog-container//div[contains(@class, 'table-title')]")).isEmpty();
    }

    protected EditBIEPage createInheritedBIE(ViewEditBIEPage viewEditBIEPage,
                                             BusinessContextObject businessContext,
                                             AppUserObject owner,
                                             ASCCPObject asccp) {
        return createInheritedBIE(viewEditBIEPage, businessContext, owner, owner, asccp);
    }

    protected EditBIEPage createInheritedBIE(ViewEditBIEPage viewEditBIEPage,
                                             BusinessContextObject businessContext,
                                             AppUserObject baseOwner,
                                             AppUserObject createdOwner,
                                             ASCCPObject asccp) {
        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(businessContext.getName());
        viewEditBIEPage.setOwner(baseOwner.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(tr);
        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(businessContext.getName());
        viewEditBIEPage.setOwner(createdOwner.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        WebElement inheritedBieTr = findInheritedBieRecord(viewEditBIEPage);
        assertNotNull(inheritedBieTr);
        return viewEditBIEPage.openEditBIEPage(inheritedBieTr);
    }

    protected WebElement findInheritedBieRecord(ViewEditBIEPage viewEditBIEPage) {
        for (int i = 1; i <= viewEditBIEPage.getTotalNumberOfItems(); i++) {
            WebElement tr = viewEditBIEPage.getTableRecordAtIndex(i);
            WebElement td = viewEditBIEPage.getColumnByName(tr, "den");
            if (getText(td).contains("Based on:")) {
                return tr;
            }
        }
        return null;
    }

    protected WebElement getDependencyRowByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-dialog-container//td//*[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    protected void assertBIEDeleted(TopLevelASBIEPObject topLevelASBIEP) {
        assertNull(getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelASBIEP.getTopLevelAsbiepId()));
    }

    protected static class TestGraph {
        final AppUserObject endUser;
        final AppUserObject otherEndUser;
        final BusinessContextObject businessContext;
        final ReleaseObject release;
        final NamespaceObject namespace;
        final LibraryObject library;
        final ASCCPObject bomAsccp;
        final ASCCPObject bomHeaderAsccp;
        final ASCCPObject securityClassificationAsccp;
        final TopLevelASBIEPObject primaryBaseBIE;
        final TopLevelASBIEPObject primaryDerivedBIE;
        final TopLevelASBIEPObject sharedHeaderBaseBIE;
        final TopLevelASBIEPObject sharedHeaderDerivedBIE;
        final TopLevelASBIEPObject sharedReusableClassificationBIE;
        final CodeListObject primaryAssignedCodeList;
        final CodeListObject secondaryAssignedCodeList;
        final HomePage homePage;

        TestGraph(AppUserObject endUser,
                  AppUserObject otherEndUser,
                  BusinessContextObject businessContext,
                  ReleaseObject release,
                  NamespaceObject namespace,
                  LibraryObject library,
                  ASCCPObject bomAsccp,
                  ASCCPObject bomHeaderAsccp,
                  ASCCPObject securityClassificationAsccp,
                  TopLevelASBIEPObject primaryBaseBIE,
                  TopLevelASBIEPObject primaryDerivedBIE,
                  TopLevelASBIEPObject sharedHeaderBaseBIE,
                  TopLevelASBIEPObject sharedHeaderDerivedBIE,
                  TopLevelASBIEPObject sharedReusableClassificationBIE,
                  CodeListObject primaryAssignedCodeList,
                  CodeListObject secondaryAssignedCodeList,
                  HomePage homePage) {
            this.endUser = endUser;
            this.otherEndUser = otherEndUser;
            this.businessContext = businessContext;
            this.release = release;
            this.namespace = namespace;
            this.library = library;
            this.bomAsccp = bomAsccp;
            this.bomHeaderAsccp = bomHeaderAsccp;
            this.securityClassificationAsccp = securityClassificationAsccp;
            this.primaryBaseBIE = primaryBaseBIE;
            this.primaryDerivedBIE = primaryDerivedBIE;
            this.sharedHeaderBaseBIE = sharedHeaderBaseBIE;
            this.sharedHeaderDerivedBIE = sharedHeaderDerivedBIE;
            this.sharedReusableClassificationBIE = sharedReusableClassificationBIE;
            this.primaryAssignedCodeList = primaryAssignedCodeList;
            this.secondaryAssignedCodeList = secondaryAssignedCodeList;
            this.homePage = homePage;
        }
    }
}
