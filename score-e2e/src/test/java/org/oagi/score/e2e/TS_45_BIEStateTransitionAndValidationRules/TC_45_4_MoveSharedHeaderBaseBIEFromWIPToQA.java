package org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.page.bie.EditBIEPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_45_4_MoveSharedHeaderBaseBIEFromWIPToQA extends TS45Base {

    @Test
    @DisplayName("TC_45_4_1_and_TC_45_4_2_and_TC_45_4_3_and_TC_45_4_4")
    public void move_shared_header_base_bie_from_wip_to_qa() {
        TestGraph graph = createTestGraph();

        EditBIEPage editBIEPage = openEditBIEPage(graph.homePage, graph.sharedHeaderBaseBIE);
        openMoveToQADialog(editBIEPage);

        assertTrue(!getDependencyDialogUpdateButton().isEnabled());
        assertDependencySummaryContains(VALIDATION_SUMMARY_QA);

        assertDependencyRowDisplayed(graph.secondaryAssignedCodeList.getName());
        assertDependencyRowMessage(graph.secondaryAssignedCodeList.getName(), CODE_LIST_MUST_BE_IN_QA_MESSAGE);

        selectDependencyRow(graph.secondaryAssignedCodeList.getName());
        assertRemainingMessagesAfterSelection(
                List.of(),
                List.of(),
                BIE_MUST_BE_IN_QA_MESSAGE,
                CODE_LIST_MUST_BE_IN_QA_MESSAGE);
        assertTrue(getDependencyDialogUpdateButton().isEnabled());

        confirmDependencyDialogUpdate();

        assertBIEState(graph.sharedHeaderBaseBIE, "QA");
        assertBIEState(graph.sharedHeaderDerivedBIE, "WIP");
        assertBIEState(graph.primaryBaseBIE, "WIP");
        assertBIEState(graph.primaryDerivedBIE, "WIP");
        assertCodeListState(graph.secondaryAssignedCodeList, "QA");
        assertCodeListState(graph.primaryAssignedCodeList, "WIP");
    }
}
