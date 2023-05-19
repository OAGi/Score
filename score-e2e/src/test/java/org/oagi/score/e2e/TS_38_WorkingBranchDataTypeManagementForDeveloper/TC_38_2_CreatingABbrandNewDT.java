package org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.DTObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
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
public class TC_38_2_CreatingABbrandNewDT extends BaseTest {

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
        ReleaseObject branch;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        assertTrue(developerA.isDeveloper());
        DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Numeric. Type", "Working");
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
        assertTrue(dtViewEditPage.getDENFieldValue().equals(den));
        assertDisabled(dtViewEditPage.getDENField());
        assertDisabled(dtViewEditPage.getSixHexadecimalIdentifierField());
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceById(baseDT.getNamespaceId());
        assertTrue(dtViewEditPage.getNamespaceFieldValue().equals(namespace.getUri()));
        assertEquals(baseDT.getDefinition(), dtViewEditPage.getDefinitionFieldValue());
        assertEquals(null, dtViewEditPage.getDefinitionSourceFieldValue());
        assertTrue(dtViewEditPage.getContentComponentDefinitionFieldValue().equals(baseDT.getContentComponentDefinition()));
        AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + baseDT.getDen());
        assertEquals(null, getText(addCommentDialog.getCommentField()));
        assertDoesNotThrow(() -> addCommentDialog.setComment("test comment"));
        escape(getDriver());
        dtViewEditPage.showValueDomain();
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
        ReleaseObject publishedRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
        viewEditCoreComponentPage.setBranch(publishedRelease.getReleaseNumber());
        viewEditCoreComponentPage.setDEN(newQualifier + "_" + baseDT.getDen());
        viewEditCoreComponentPage.hitSearchButton();
        assertThrows(TimeoutException.class, () -> viewEditCoreComponentPage.getTableRecordByValue(newQualifier + "_" + baseDT.getDen()));
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
