package org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.page.bie.EditBIEPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_45_9_BlockBaseBIEFromWIPToQAByDifferentOwnerAssignedCodeList extends TS45Base {

    @Test
    @DisplayName("TC_45_9_1_and_TC_45_9_2_and_TC_45_9_3")
    public void block_base_bie_wip_to_qa_by_different_owner_assigned_code_list() {
        TestGraph graph = createTestGraphWithCrossOwnerPrimaryCodeList();

        EditBIEPage editBIEPage = openEditBIEPage(graph.homePage, graph.primaryBaseBIE);
        openMoveToQADialog(editBIEPage);

        assertTrue(!getDependencyDialogUpdateButton().isEnabled());
        assertDependencySummaryContains(VALIDATION_SUMMARY_QA);

        assertDependencyRowDisplayed(graph.sharedHeaderBaseBIE.getVersion());
        assertDependencyRowMessage(graph.sharedHeaderBaseBIE.getVersion(), BIE_MUST_BE_IN_QA_MESSAGE);
        selectDependencyRow(graph.sharedHeaderBaseBIE.getVersion());

        assertDependencyRowDisplayed(graph.sharedReusableClassificationBIE.getVersion());
        assertDependencyRowMessage(graph.sharedReusableClassificationBIE.getVersion(), BIE_MUST_BE_IN_QA_MESSAGE);
        selectDependencyRow(graph.sharedReusableClassificationBIE.getVersion());

        assertDependencyRowDisplayed(graph.secondaryAssignedCodeList.getName());
        assertDependencyRowMessage(graph.secondaryAssignedCodeList.getName(), CODE_LIST_MUST_BE_IN_QA_MESSAGE);
        selectDependencyRow(graph.secondaryAssignedCodeList.getName());

        assertDependencyRowDisplayed(graph.primaryAssignedCodeList.getName());
        assertDependencyRowMessage(graph.primaryAssignedCodeList.getName(), codeListOwnershipMessage(graph.otherEndUser));
        assertDependencyRowNotSelectable(graph.primaryAssignedCodeList.getName());

        assertTrue(!getDependencyDialogUpdateButton().isEnabled());

        cancelDependencyDialog();

        assertBIEState(graph.primaryBaseBIE, "WIP");
        assertBIEState(graph.sharedHeaderBaseBIE, "WIP");
        assertBIEState(graph.sharedReusableClassificationBIE, "WIP");
        assertCodeListState(graph.primaryAssignedCodeList, "WIP");
        assertCodeListState(graph.secondaryAssignedCodeList, "WIP");
    }
}
