package org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_45_1_MoveDerivedBIEFromWIPToQA extends TS45Base {

    @Test
    @DisplayName("TC_45_1_1_and_TC_45_1_2_and_TC_45_1_3_and_TC_45_1_4_and_TC_45_1_5")
    public void move_derived_bie_from_wip_to_qa() {
        TestGraph graph = createTestGraph();

        EditBIEPage editBIEPage = openEditBIEPage(graph.homePage, graph.primaryDerivedBIE);
        openMoveToQADialog(editBIEPage);

        WebElement updateButton = getDependencyDialogUpdateButton();
        assertTrue(!updateButton.isEnabled());
        assertDependencySummaryContains(VALIDATION_SUMMARY_QA);

        assertDependencyRowDisplayed(graph.sharedHeaderBaseBIE.getVersion());
        assertDependencyRowDisplayed(graph.sharedHeaderDerivedBIE.getVersion());
        assertDependencyRowDisplayed(graph.primaryBaseBIE.getVersion());
        assertDependencyRowDisplayed(graph.sharedReusableClassificationBIE.getVersion());
        assertDependencyRowDisplayed(graph.primaryAssignedCodeList.getName());
        assertDependencyRowDisplayed(graph.secondaryAssignedCodeList.getName());
        assertDependencyRowMessage(graph.sharedHeaderBaseBIE.getVersion(), BIE_MUST_BE_IN_QA_MESSAGE);
        assertDependencyRowMessage(graph.sharedHeaderDerivedBIE.getVersion(), BIE_MUST_BE_IN_QA_MESSAGE);
        assertDependencyRowMessage(graph.primaryBaseBIE.getVersion(), BIE_MUST_BE_IN_QA_MESSAGE);
        assertDependencyRowMessage(graph.sharedReusableClassificationBIE.getVersion(), BIE_MUST_BE_IN_QA_MESSAGE);
        assertDependencyRowMessage(graph.primaryAssignedCodeList.getName(), CODE_LIST_MUST_BE_IN_QA_MESSAGE);
        assertDependencyRowMessage(graph.secondaryAssignedCodeList.getName(), CODE_LIST_MUST_BE_IN_QA_MESSAGE);

        selectDependencyRow(graph.sharedHeaderBaseBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(
                        graph.sharedHeaderDerivedBIE.getVersion(),
                        graph.primaryBaseBIE.getVersion(),
                        graph.sharedReusableClassificationBIE.getVersion()),
                List.of(
                        graph.primaryAssignedCodeList.getName(),
                        graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_QA_MESSAGE,
                CODE_LIST_MUST_BE_IN_QA_MESSAGE);

        selectDependencyRow(graph.sharedHeaderDerivedBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(
                        graph.primaryBaseBIE.getVersion(),
                        graph.sharedReusableClassificationBIE.getVersion()),
                List.of(
                        graph.primaryAssignedCodeList.getName(),
                        graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_QA_MESSAGE,
                CODE_LIST_MUST_BE_IN_QA_MESSAGE);

        selectDependencyRow(graph.primaryBaseBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(graph.sharedReusableClassificationBIE.getVersion()),
                List.of(
                        graph.primaryAssignedCodeList.getName(),
                        graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_QA_MESSAGE,
                CODE_LIST_MUST_BE_IN_QA_MESSAGE);

        selectDependencyRow(graph.sharedReusableClassificationBIE.getVersion());
        assertRemainingMessagesAfterSelection(
                List.of(),
                List.of(
                        graph.primaryAssignedCodeList.getName(),
                        graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_QA_MESSAGE,
                CODE_LIST_MUST_BE_IN_QA_MESSAGE);

        selectDependencyRow(graph.primaryAssignedCodeList.getName());
        assertRemainingMessagesAfterSelection(
                List.of(),
                List.of(graph.secondaryAssignedCodeList.getName()),
                BIE_MUST_BE_IN_QA_MESSAGE,
                CODE_LIST_MUST_BE_IN_QA_MESSAGE);
        assertTrue(!getDependencyDialogUpdateButton().isEnabled());

        selectDependencyRow(graph.secondaryAssignedCodeList.getName());
        assertTrue(getDependencyDialogUpdateButton().isEnabled());

        confirmDependencyDialogUpdate();

        assertBIEState(graph.primaryDerivedBIE, "QA");
        assertBIEState(graph.primaryBaseBIE, "QA");
        assertBIEState(graph.sharedHeaderDerivedBIE, "QA");
        assertBIEState(graph.sharedHeaderBaseBIE, "QA");
        assertBIEState(graph.sharedReusableClassificationBIE, "QA");
        assertCodeListState(graph.primaryAssignedCodeList, "QA");
        assertCodeListState(graph.secondaryAssignedCodeList, "QA");
    }
}
