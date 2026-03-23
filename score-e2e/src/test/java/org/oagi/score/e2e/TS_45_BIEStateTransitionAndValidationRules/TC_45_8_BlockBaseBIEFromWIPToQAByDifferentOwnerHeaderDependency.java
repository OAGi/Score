package org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.page.bie.EditBIEPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_45_8_BlockBaseBIEFromWIPToQAByDifferentOwnerHeaderDependency extends TS45Base {

    @Test
    @DisplayName("TC_45_8_1_and_TC_45_8_2_and_TC_45_8_3")
    public void block_base_bie_wip_to_qa_by_different_owner_header_dependency() {
        TestGraph graph = createTestGraphWithCrossOwnerHeaderDependency();

        EditBIEPage editBIEPage = openEditBIEPage(graph.homePage, graph.primaryBaseBIE);
        openMoveToQADialog(editBIEPage);

        assertTrue(!getDependencyDialogUpdateButton().isEnabled());
        assertDependencySummaryContains(VALIDATION_SUMMARY_QA);

        assertDependencyRowDisplayed(graph.sharedHeaderBaseBIE.getVersion());
        assertDependencyRowMessage(graph.sharedHeaderBaseBIE.getVersion(), bieOwnershipMessage(graph.otherEndUser));
        assertDependencyRowNotSelectable(graph.sharedHeaderBaseBIE.getVersion());

        assertDependencyRowDisplayed(graph.sharedReusableClassificationBIE.getVersion());
        assertDependencyRowMessage(graph.sharedReusableClassificationBIE.getVersion(), BIE_MUST_BE_IN_QA_MESSAGE);
        selectDependencyRow(graph.sharedReusableClassificationBIE.getVersion());

        assertDependencyRowDisplayed(graph.primaryAssignedCodeList.getName());
        assertDependencyRowMessage(graph.primaryAssignedCodeList.getName(), CODE_LIST_MUST_BE_IN_QA_MESSAGE);
        selectDependencyRow(graph.primaryAssignedCodeList.getName());

        assertTrue(!getDependencyDialogUpdateButton().isEnabled());
        assertDependencyRowMessage(graph.sharedHeaderBaseBIE.getVersion(), bieOwnershipMessage(graph.otherEndUser));

        cancelDependencyDialog();

        assertBIEState(graph.primaryBaseBIE, "WIP");
        assertBIEState(graph.sharedHeaderBaseBIE, "WIP");
        assertBIEState(graph.sharedReusableClassificationBIE, "WIP");
        assertCodeListState(graph.primaryAssignedCodeList, "WIP");
        assertCodeListState(graph.secondaryAssignedCodeList, "QA");
    }
}
