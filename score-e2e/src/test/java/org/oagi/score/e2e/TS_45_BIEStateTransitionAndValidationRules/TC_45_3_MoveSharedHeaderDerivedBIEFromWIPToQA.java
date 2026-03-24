package org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.page.bie.EditBIEPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_45_3_MoveSharedHeaderDerivedBIEFromWIPToQA extends TS45Base {

    @Test
    @DisplayName("TC_45_3_1_and_TC_45_3_2_and_TC_45_3_3")
    public void move_shared_header_derived_bie_from_wip_to_qa() {
        TestGraph graph = createTestGraph();

        EditBIEPage editBIEPage = openEditBIEPage(graph.homePage, graph.sharedHeaderDerivedBIE);
        openMoveToQADialog(editBIEPage);

        assertTrue(!getDependencyDialogUpdateButton().isEnabled());
        assertDependencySummaryContains(VALIDATION_SUMMARY_QA);

        assertDependencyRowDisplayed(graph.sharedHeaderBaseBIE.getVersion());
        assertDependencyRowDisplayed(graph.secondaryAssignedCodeList.getName());
        assertDependencyRowMessage(graph.sharedHeaderBaseBIE.getVersion(), BIE_MUST_BE_IN_QA_MESSAGE);
        assertDependencyRowMessage(graph.secondaryAssignedCodeList.getName(), CODE_LIST_MUST_BE_IN_QA_MESSAGE);

        selectDependencyRow(graph.sharedHeaderBaseBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(),
                List.of(graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_QA_MESSAGE,
                CODE_LIST_MUST_BE_IN_QA_MESSAGE);
        assertTrue(!getDependencyDialogUpdateButton().isEnabled());

        selectDependencyRow(graph.secondaryAssignedCodeList.getName());
        assertTrue(getDependencyDialogUpdateButton().isEnabled());

        confirmDependencyDialogUpdate();

        assertBIEState(graph.sharedHeaderDerivedBIE, "QA");
        assertBIEState(graph.sharedHeaderBaseBIE, "QA");
        assertBIEState(graph.primaryBaseBIE, "WIP");
        assertBIEState(graph.primaryDerivedBIE, "WIP");
        assertCodeListState(graph.secondaryAssignedCodeList, "QA");
        assertCodeListState(graph.primaryAssignedCodeList, "WIP");
    }
}
