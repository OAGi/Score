package org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.page.bie.EditBIEPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_45_7_MoveSharedHeaderBaseBIEFromQAToWIPWithSameOwnerDependentBIEs extends TS45Base {

    @Test
    @DisplayName("TC_45_7_1_and_TC_45_7_2_and_TC_45_7_3")
    public void move_shared_header_base_bie_from_qa_to_wip() {
        TestGraph graph = createTestGraph();

        setBIEState(graph.primaryBaseBIE, "QA");
        setBIEState(graph.primaryDerivedBIE, "QA");
        setBIEState(graph.sharedHeaderBaseBIE, "QA");
        setBIEState(graph.sharedHeaderDerivedBIE, "QA");
        setBIEState(graph.sharedReusableClassificationBIE, "WIP");
        setCodeListState(graph.primaryAssignedCodeList, "QA");
        setCodeListState(graph.secondaryAssignedCodeList, "QA");

        EditBIEPage editBIEPage = openEditBIEPage(graph.homePage, graph.sharedHeaderBaseBIE);
        openBackToWIPDialog(editBIEPage);

        assertTrue(!getDependencyDialogUpdateButton().isEnabled());
        assertDependencySummaryContains(VALIDATION_SUMMARY_WIP);

        assertDependencyRowDisplayed(graph.primaryBaseBIE.getVersion());
        assertDependencyRowDisplayed(graph.sharedHeaderDerivedBIE.getVersion());
        assertDependencyRowDisplayed(graph.primaryDerivedBIE.getVersion());
        assertDependencyRowMessage(graph.primaryBaseBIE.getVersion(), BIE_MUST_BE_IN_WIP_MESSAGE);
        assertDependencyRowMessage(graph.sharedHeaderDerivedBIE.getVersion(), BIE_MUST_BE_IN_WIP_MESSAGE);
        assertDependencyRowMessage(graph.primaryDerivedBIE.getVersion(), BIE_MUST_BE_IN_WIP_MESSAGE);

        selectDependencyRow(graph.primaryBaseBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(graph.sharedHeaderDerivedBIE.getVersion(), graph.primaryDerivedBIE.getVersion()),
                List.of(),
                BIE_MUST_BE_IN_WIP_MESSAGE,
                CODE_LIST_MUST_BE_IN_QA_MESSAGE);

        selectDependencyRow(graph.sharedHeaderDerivedBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(graph.primaryDerivedBIE.getVersion()),
                List.of(),
                BIE_MUST_BE_IN_WIP_MESSAGE,
                CODE_LIST_MUST_BE_IN_QA_MESSAGE);
        assertTrue(!getDependencyDialogUpdateButton().isEnabled());

        selectDependencyRow(graph.primaryDerivedBIE.getVersion());
        assertTrue(getDependencyDialogUpdateButton().isEnabled());

        confirmDependencyDialogUpdate();

        assertBIEState(graph.sharedHeaderBaseBIE, "WIP");
        assertBIEState(graph.primaryBaseBIE, "WIP");
        assertBIEState(graph.sharedHeaderDerivedBIE, "WIP");
        assertBIEState(graph.primaryDerivedBIE, "WIP");
    }
}
