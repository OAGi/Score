package org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.page.bie.EditBIEPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_45_5_MoveSharedReusableClassificationBIEFromWIPToQA extends TS45Base {

    @Test
    @DisplayName("TC_45_5_1_and_TC_45_5_2")
    public void move_shared_reusable_classification_bie_from_wip_to_qa() {
        TestGraph graph = createTestGraph();

        EditBIEPage editBIEPage = openEditBIEPage(graph.homePage, graph.sharedReusableClassificationBIE);
        openMoveToQADialog(editBIEPage);

        assertTrue(!hasValidationSummary());
        assertTrue(!hasDependencyTable());

        confirmDependencyDialogUpdate();

        assertBIEState(graph.sharedReusableClassificationBIE, "QA");
        assertBIEState(graph.primaryBaseBIE, "WIP");
        assertBIEState(graph.primaryDerivedBIE, "WIP");
        assertBIEState(graph.sharedHeaderBaseBIE, "WIP");
        assertBIEState(graph.sharedHeaderDerivedBIE, "WIP");
    }
}
