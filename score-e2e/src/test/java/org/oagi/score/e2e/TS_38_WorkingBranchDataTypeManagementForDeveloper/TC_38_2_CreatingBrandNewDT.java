package org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper;

import org.apache.commons.lang3.StringUtils;
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
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_38_2_CreatingBrandNewDT extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_38_2_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        LibraryObject library;
        ReleaseObject branch;
        DTObject baseDT;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            baseDT = getAPIFactory().getCoreComponentAPI().getBDTByDENAndReleaseNum(
                    library, "Open_ Number. Type", branch.getReleaseNumber()).get(0);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        assertTrue(developerA.isDeveloper());

        DTViewEditPage dtViewEditPage = viewEditDataTypePage.createDT(baseDT.getDen(), branch.getReleaseNumber());
        assertTrue(dtViewEditPage.getBasedDataTypeFieldValue().equals(baseDT.getDen()));
        assertDisabled(dtViewEditPage.getDataTypeTermField());
        assertTrue(dtViewEditPage.getDataTypeTermFieldValue().equals(baseDT.getDataTypeTerm()));
        assertDisabled(dtViewEditPage.getDataTypeTermField());
        assertTrue(dtViewEditPage.getRepresentationTermFieldValue().equals(baseDT.getRepresentationTerm()));
        assertDisabled(dtViewEditPage.getRepresentationTermField());
        String den = "";
        if (baseDT.getQualifier() != null) {
            assertTrue(dtViewEditPage.getQualifierFieldValue().equals(baseDT.getQualifier()));
            den = baseDT.getQualifier() + "_ " + baseDT.getDataTypeTerm() + ". Type";
        } else {
            assertEquals(null, dtViewEditPage.getQualifierFieldValue());
            den = baseDT.getDataTypeTerm() + ". Type";
        }
        assertTrue(dtViewEditPage.getDENFieldValue().equals(den));
        assertDisabled(dtViewEditPage.getDENField());
        assertDisabled(dtViewEditPage.getSixHexadecimalIdentifierField());
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceById(baseDT.getNamespaceId());
        assertTrue(dtViewEditPage.getNamespaceFieldValue().equals(namespace.getUri()));
        assertEquals(baseDT.getDefinition(), dtViewEditPage.getDefinitionFieldValue());
        assertEquals(null, dtViewEditPage.getDefinitionSourceFieldValue());
        assertTrue(StringUtils.equals(dtViewEditPage.getContentComponentDefinitionFieldValue(), baseDT.getContentComponentDefinition()));
        AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + baseDT.getDen());
        assertEquals(null, getText(addCommentDialog.getCommentField()));
        assertDoesNotThrow(() -> addCommentDialog.setComment("test comment"));
        escape(getDriver());
        dtViewEditPage.showValueDomain();
        assertDoesNotThrow(() -> dtViewEditPage.getValueDomainByTypeNameAndXSDExpression("Primitive", "Float", "float"));
        assertDoesNotThrow(() -> dtViewEditPage.getValueDomainByTypeNameAndXSDExpression("Primitive", "Decimal", "decimal"));
        assertDoesNotThrow(() -> dtViewEditPage.getValueDomainByTypeNameAndXSDExpression("Primitive", "Double", "float, double"));

        assertDisabled(dtViewEditPage.getCheckboxForValueDomainByTypeAndName("Primitive", "Float"));
        assertEquals("Working", dtViewEditPage.getReleaseFieldValue());
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
        ReleaseObject publishedRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
        viewEditDataTypePage.setBranch(publishedRelease.getReleaseNumber());
        viewEditDataTypePage.setDEN(newQualifier + "_" + baseDT.getDen());
        viewEditDataTypePage.hitSearchButton();
        assertThrows(TimeoutException.class, () -> viewEditDataTypePage.getTableRecordByValue(newQualifier + "_ " + baseDT.getDen()));
    }

    @Test
    @DisplayName("TC_38_2_TA_2")
    public void test_TA_2() {
        AppUserObject developerA;
        ReleaseObject branch;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        viewEditDataTypePage.setBranch(branch.getReleaseNumber());
        assertThrows(TimeoutException.class, () -> viewEditDataTypePage.getNewDataTypeButton());
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
