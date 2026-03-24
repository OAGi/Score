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
public class TC_45_11_AdministratorDiscardsDerivedBIEFromProduction extends TS45Base {

    @Test
    @DisplayName("TC_45_11_1_and_TC_45_11_2_and_TC_45_11_3_and_TC_45_11_4")
    public void administrator_discards_derived_bie_from_production() {
        TestGraph graph = createTestGraph();

        setBIEState(graph.primaryBaseBIE, "Production");
        setBIEState(graph.primaryDerivedBIE, "Production");
        setBIEState(graph.sharedHeaderBaseBIE, "Production");
        setBIEState(graph.sharedHeaderDerivedBIE, "Production");
        setBIEState(graph.sharedReusableClassificationBIE, "Production");
        setCodeListState(graph.primaryAssignedCodeList, "Production");
        setCodeListState(graph.secondaryAssignedCodeList, "Production");

        AppUserObject admin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(admin);

        HomePage adminHomePage = graph.homePage.logout().signIn(admin.getLoginId(), admin.getPassword());
        EditBIEPage editBIEPage = openEditBIEPage(adminHomePage, graph.primaryDerivedBIE);
        openDiscardDialog(editBIEPage);

        assertTrue(!hasValidationSummary());
        assertTrue(!hasDependencyTable());
        confirmDependencyDialogDiscard();

        assertBIEDeleted(graph.primaryDerivedBIE);
        assertBIEState(graph.primaryBaseBIE, "Production");
        assertBIEState(graph.sharedHeaderBaseBIE, "Production");
        assertBIEState(graph.sharedHeaderDerivedBIE, "Production");
        assertBIEState(graph.sharedReusableClassificationBIE, "Production");
        assertCodeListState(graph.primaryAssignedCodeList, "Production");
        assertCodeListState(graph.secondaryAssignedCodeList, "Production");
    }
}
