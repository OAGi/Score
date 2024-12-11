package org.oagi.score.e2e.TS_41_ReleaseBranchDataTypeManagementForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_41_2_CreatingBrandNewDT extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_41_2_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        LibraryObject library;
        ReleaseObject branch;
        NamespaceObject namespace;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        assertFalse(endUserA.isDeveloper());
        DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Numeric. Type", "10.8.4");
        DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.createDT(baseDT.getDen(), branch.getReleaseNumber());
        assertTrue(dtViewEditPage.getBasedDataTypeFieldValue().equals(baseDT.getDen()));
        assertDisabled(dtViewEditPage.getDataTypeTermField());
        assertTrue(dtViewEditPage.getDataTypeTermFieldValue().equals(baseDT.getDataTypeTerm()));
        assertDisabled(dtViewEditPage.getDataTypeTermField());
        assertTrue(dtViewEditPage.getRepresentationTermFieldValue().equals(baseDT.getRepresentationTerm()));
        assertDisabled(dtViewEditPage.getRepresentationTermField());
        String den = "";
        if (baseDT.getQualifier() != null){
            assertTrue(dtViewEditPage.getQualifierFieldValue().equals(baseDT.getQualifier()));
            den = baseDT.getQualifier() + "" + baseDT.getDataTypeTerm() + ". Type";
        } else{
            assertEquals(null, dtViewEditPage.getQualifierFieldValue());
            den = baseDT.getDataTypeTerm() + ". Type";
        }
        dtViewEditPage.setNamespace(namespace);
        assertTrue(dtViewEditPage.getDENFieldValue().equals(den));
        assertDisabled(dtViewEditPage.getDENField());
        assertDisabled(dtViewEditPage.getSixHexadecimalIdentifierField());
        assertEquals(baseDT.getDefinition(), dtViewEditPage.getDefinitionFieldValue());
        assertEquals(null, dtViewEditPage.getDefinitionSourceFieldValue());
        assertTrue(dtViewEditPage.getContentComponentDefinitionFieldValue().equals(baseDT.getContentComponentDefinition()));
        AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + baseDT.getDen());
        assertEquals(null, getText(addCommentDialog.getCommentField()));
        assertDoesNotThrow(() -> addCommentDialog.setComment("test comment"));
        escape(getDriver());
        dtViewEditPage.showValueDomain();
        assertDoesNotThrow(() -> dtViewEditPage.getValueDomainByTypeNameAndXSDExpression("Primitive", "Float", "float"));
        assertDoesNotThrow(() -> dtViewEditPage.getValueDomainByTypeNameAndXSDExpression("Primitive", "Decimal", "decimal"));
        assertDoesNotThrow(() -> dtViewEditPage.getValueDomainByTypeNameAndXSDExpression("Primitive", "Double", "double, float"));

        assertDisabled(dtViewEditPage.getCheckboxForValueDomainByTypeAndName("Primitive", "Float"));
        assertEquals(branch.getReleaseNumber(), dtViewEditPage.getReleaseFieldValue());
        assertEquals("1", dtViewEditPage.getRevisionFieldValue());
        assertEnabled(dtViewEditPage.getQualifierField());
        String newQualifier = "newBDT";
        dtViewEditPage.setQualifier(newQualifier);
        assertEquals(null, getText(dtViewEditPage.getSixHexadecimalIdentifierField()));
        assertEnabled(dtViewEditPage.getDefinitionField());
        dtViewEditPage.setDefinition("");
        assertEnabled(dtViewEditPage.getDefinitionSourceField());
        dtViewEditPage.setDefinitionSource("");
        assertEnabled(dtViewEditPage.getContentComponentDefinitionField());
        dtViewEditPage.setContentComponentDefinition("");
        click(dtViewEditPage.getUpdateButton(true));
        dtViewEditPage.hitUpdateAnywayButton();
        homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        viewEditCoreComponentPage.setBranch(workingRelease.getReleaseNumber());
        viewEditCoreComponentPage.setDEN(newQualifier + "_" + baseDT.getDen());
        viewEditCoreComponentPage.hitSearchButton();
        assertThrows(TimeoutException.class, () -> viewEditCoreComponentPage.getTableRecordByValue(newQualifier + "_ " + baseDT.getDen()));
    }
    @Test
    @DisplayName("TC_41_2_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        ReleaseObject branch;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        assertThrows(TimeoutException.class, () -> viewEditCoreComponentPage.getCreateDTButton());
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }
}
