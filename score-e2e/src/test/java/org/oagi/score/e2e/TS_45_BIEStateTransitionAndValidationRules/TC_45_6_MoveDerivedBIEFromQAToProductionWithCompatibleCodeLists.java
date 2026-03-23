package org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.page.bie.EditBIEPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_45_6_MoveDerivedBIEFromQAToProductionWithCompatibleCodeLists extends TS45Base {

    @Test
    @DisplayName("TC_45_6_1_and_TC_45_6_2_and_TC_45_6_3")
    public void move_primary_derived_bie_from_qa_to_production() {
        TestGraph graph = createTestGraph();

        setBIEState(graph.primaryBaseBIE, "QA");
        setBIEState(graph.primaryDerivedBIE, "QA");
        setBIEState(graph.sharedHeaderBaseBIE, "QA");
        setBIEState(graph.sharedHeaderDerivedBIE, "QA");
        setBIEState(graph.sharedReusableClassificationBIE, "QA");
        setCodeListState(graph.primaryAssignedCodeList, "QA");
        setCodeListState(graph.secondaryAssignedCodeList, "QA");

        EditBIEPage editBIEPage = openEditBIEPage(graph.homePage, graph.primaryDerivedBIE);
        openMoveToProductionDialog(editBIEPage);

        assertTrue(!getDependencyDialogUpdateButton().isEnabled());
        assertDependencySummaryContains(VALIDATION_SUMMARY_PRODUCTION);

        assertDependencyRowDisplayed(graph.sharedHeaderBaseBIE.getVersion());
        assertDependencyRowDisplayed(graph.sharedHeaderDerivedBIE.getVersion());
        assertDependencyRowDisplayed(graph.primaryBaseBIE.getVersion());
        assertDependencyRowDisplayed(graph.sharedReusableClassificationBIE.getVersion());
        assertDependencyRowDisplayed(graph.primaryAssignedCodeList.getName());
        assertDependencyRowDisplayed(graph.secondaryAssignedCodeList.getName());
        assertDependencyRowMessage(graph.sharedHeaderBaseBIE.getVersion(), BIE_MUST_BE_IN_PRODUCTION_MESSAGE);
        assertDependencyRowMessage(graph.sharedHeaderDerivedBIE.getVersion(), BIE_MUST_BE_IN_PRODUCTION_MESSAGE);
        assertDependencyRowMessage(graph.primaryBaseBIE.getVersion(), BIE_MUST_BE_IN_PRODUCTION_MESSAGE);
        assertDependencyRowMessage(graph.sharedReusableClassificationBIE.getVersion(), BIE_MUST_BE_IN_PRODUCTION_MESSAGE);
        assertDependencyRowMessage(graph.primaryAssignedCodeList.getName(), CODE_LIST_MUST_BE_IN_PRODUCTION_MESSAGE);
        assertDependencyRowMessage(graph.secondaryAssignedCodeList.getName(), CODE_LIST_MUST_BE_IN_PRODUCTION_MESSAGE);

        selectDependencyRow(graph.sharedHeaderBaseBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(graph.sharedHeaderDerivedBIE.getVersion(), graph.primaryBaseBIE.getVersion(),
                        graph.sharedReusableClassificationBIE.getVersion()),
                List.of(graph.primaryAssignedCodeList.getName(), graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_PRODUCTION_MESSAGE,
                CODE_LIST_MUST_BE_IN_PRODUCTION_MESSAGE);

        selectDependencyRow(graph.sharedHeaderDerivedBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(graph.primaryBaseBIE.getVersion(), graph.sharedReusableClassificationBIE.getVersion()),
                List.of(graph.primaryAssignedCodeList.getName(), graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_PRODUCTION_MESSAGE,
                CODE_LIST_MUST_BE_IN_PRODUCTION_MESSAGE);

        selectDependencyRow(graph.primaryBaseBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(graph.sharedReusableClassificationBIE.getVersion()),
                List.of(graph.primaryAssignedCodeList.getName(), graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_PRODUCTION_MESSAGE,
                CODE_LIST_MUST_BE_IN_PRODUCTION_MESSAGE);

        selectDependencyRow(graph.sharedReusableClassificationBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(),
                List.of(graph.primaryAssignedCodeList.getName(), graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_PRODUCTION_MESSAGE,
                CODE_LIST_MUST_BE_IN_PRODUCTION_MESSAGE);

        selectDependencyRow(graph.primaryAssignedCodeList.getName());
        assertRemainingMessagesAfterSelection(
                List.of(),
                List.of(graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_PRODUCTION_MESSAGE,
                CODE_LIST_MUST_BE_IN_PRODUCTION_MESSAGE);
        assertTrue(!getDependencyDialogUpdateButton().isEnabled());

        selectDependencyRow(graph.secondaryAssignedCodeList.getName());
        assertTrue(getDependencyDialogUpdateButton().isEnabled());

        confirmDependencyDialogUpdate();

        assertBIEState(graph.primaryDerivedBIE, "Production");
        assertBIEState(graph.primaryBaseBIE, "Production");
        assertBIEState(graph.sharedHeaderDerivedBIE, "Production");
        assertBIEState(graph.sharedHeaderBaseBIE, "Production");
        assertBIEState(graph.sharedReusableClassificationBIE, "Production");
        assertCodeListState(graph.primaryAssignedCodeList, "Production");
        assertCodeListState(graph.secondaryAssignedCodeList, "Production");
    }
}
