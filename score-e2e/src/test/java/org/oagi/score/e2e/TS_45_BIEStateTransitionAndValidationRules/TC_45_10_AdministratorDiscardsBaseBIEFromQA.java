package org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_45_10_AdministratorDiscardsBaseBIEFromQA extends TS45Base {

    @Test
    @DisplayName("TC_45_10_1_and_TC_45_10_2_and_TC_45_10_3_and_TC_45_10_4")
    public void administrator_discards_base_bie_from_qa() {
        TestGraph graph = createTestGraph();

        setBIEState(graph.primaryBaseBIE, "QA");
        setBIEState(graph.primaryDerivedBIE, "QA");
        setBIEState(graph.sharedHeaderBaseBIE, "QA");
        setBIEState(graph.sharedHeaderDerivedBIE, "QA");
        setBIEState(graph.sharedReusableClassificationBIE, "QA");
        setCodeListState(graph.primaryAssignedCodeList, "QA");
        setCodeListState(graph.secondaryAssignedCodeList, "QA");

        AppUserObject admin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(admin);

        HomePage adminHomePage = graph.homePage.logout().signIn(admin.getLoginId(), admin.getPassword());
        EditBIEPage editBIEPage = openEditBIEPage(adminHomePage, graph.primaryBaseBIE);
        openDiscardDialog(editBIEPage);

        assertTrue(!getDependencyDialogDiscardButton().isEnabled());
        assertDependencySummaryContains(VALIDATION_SUMMARY_DISCARD);

        assertDependencyRowDisplayed(graph.primaryDerivedBIE.getVersion());
        assertDependencyRowMessage(graph.primaryDerivedBIE.getVersion(), BIE_MUST_BE_DISCARDED_MESSAGE);
        assertDependencyRowNotDisplayed(graph.sharedHeaderBaseBIE.getVersion());
        assertDependencyRowNotDisplayed(graph.sharedHeaderDerivedBIE.getVersion());
        assertDependencyRowNotDisplayed(graph.sharedReusableClassificationBIE.getVersion());
        assertDependencyRowNotDisplayed(graph.primaryAssignedCodeList.getName());
        assertDependencyRowNotDisplayed(graph.secondaryAssignedCodeList.getName());

        selectDependencyRow(graph.primaryDerivedBIE.getVersion());
        assertTrue(getDependencyDialogDiscardButton().isEnabled());

        confirmDependencyDialogDiscard();

        assertBIEDeleted(graph.primaryBaseBIE);
        assertBIEDeleted(graph.primaryDerivedBIE);
        assertBIEState(graph.sharedHeaderBaseBIE, "QA");
        assertBIEState(graph.sharedHeaderDerivedBIE, "QA");
        assertBIEState(graph.sharedReusableClassificationBIE, "QA");
        assertCodeListState(graph.primaryAssignedCodeList, "QA");
        assertCodeListState(graph.secondaryAssignedCodeList, "QA");
    }
}
