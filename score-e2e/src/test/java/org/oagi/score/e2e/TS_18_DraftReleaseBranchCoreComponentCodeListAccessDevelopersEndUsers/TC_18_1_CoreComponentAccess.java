package org.oagi.score.e2e.TS_18_DraftReleaseBranchCoreComponentCodeListAccessDevelopersEndUsers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.impl.api.DSLContextAPIFactory;
import org.oagi.score.e2e.impl.page.LoginPageImpl;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_18_1_CoreComponentAccess extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private ReleaseDraftSingleton releaseDraft;

    @BeforeEach
    public void init() {
        super.init();
        this.releaseDraft = ReleaseDraftSingleton.getInstance();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    @AfterAll
    public static void cleanUp() {
        ReleaseDraftSingleton.getInstance().release();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    public void test_TA_18_1_1() {
        AppUserObject developer = releaseDraft.getDeveloper();
        AppUserObject endUser = releaseDraft.getEndUser();
        ReleaseDraftSingleton.RandomCoreComponentWithStateContainer developerCoreComponentWithStateContainer = releaseDraft.getDeveloperCoreComponentWithStateContainer();
        String existingReleaseNum = releaseDraft.getExistingReleaseNum();

        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (Map.Entry<String, ACCObject> entry : developerCoreComponentWithStateContainer.getStateACCs().entrySet()) {
            String state = entry.getKey();
            ACCObject acc = developerCoreComponentWithStateContainer.getStateACCs().get(state);
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setBranch(existingReleaseNum);
            if (!state.equalsIgnoreCase("Candidate")) {
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"" + acc.getDen() + "\")]//ancestor::tr")).size());
            } else {
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(),\"" + acc.getDen() + "\")]//ancestor::tr")).size());
            }
        }
        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setBranch(existingReleaseNum);
        assertEquals(0, getDriver().findElements(By.xpath("//button[@mattooltip=\"Create Component\"]")).size());
    }

    @Test
    public void test_TA_18_1_2() {
        AppUserObject developer = releaseDraft.getDeveloper();
        AppUserObject endUser = releaseDraft.getEndUser();
        String existingReleaseNum = releaseDraft.getExistingReleaseNum();

        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");
        ccStates.add("Deleted");
        viewEditCoreComponentPage.setBranch(existingReleaseNum);

        for (String state : ccStates) {
            viewEditCoreComponentPage.setState(state);
            viewEditCoreComponentPage.hitSearchButton();
            assertEquals(0, getDriver().findElements(By.xpath("//score-cc-list//table//tbody//tr")).size());
        }
    }

    @Test
    public void test_TA_18_1_3() {
        AppUserObject developer = releaseDraft.getDeveloper();
        AppUserObject endUser = releaseDraft.getEndUser();
        ReleaseDraftSingleton.RandomCoreComponentWithStateContainer developerCoreComponentWithStateContainer = releaseDraft.getDeveloperCoreComponentWithStateContainer();
        String existingReleaseNum = releaseDraft.getExistingReleaseNum();

        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (Map.Entry<String, ACCObject> entry : developerCoreComponentWithStateContainer.getStateACCs().entrySet()) {
            String state = entry.getKey();
            ACCObject acc = developerCoreComponentWithStateContainer.getStateACCs().get(state);
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setBranch(existingReleaseNum);
            if (!state.equalsIgnoreCase("Candidate")) {
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"" + acc.getDen() + "\")]//ancestor::tr/td[1]//label/span[1]")).size());
            } else {
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                assertEquals(1, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"" + acc.getDen() + "\")]//ancestor::tr/td[1]//label/span[1]")).size());
            }
        }
        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setBranch(existingReleaseNum);
        assertEquals(0, getDriver().findElements(By.xpath("//button[@mattooltip=\"Create Component\"]")).size());
    }

    @Test
    public void test_TA_18_1_4() {
        AppUserObject developer = releaseDraft.getDeveloper();
        AppUserObject endUser = releaseDraft.getEndUser();
        String existingReleaseNum = releaseDraft.getExistingReleaseNum();

        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");
        ccStates.add("Deleted");
        viewEditCoreComponentPage.setBranch(existingReleaseNum);

        for (String state : ccStates) {
            viewEditCoreComponentPage.setState(state);
            viewEditCoreComponentPage.hitSearchButton();
            assertEquals(0, getDriver().findElements(By.xpath("//score-cc-list//table//tbody//tr")).size());
        }

    }

    @Test
    public void test_TA_18_1_5_a_b_c() {
        AppUserObject developer = releaseDraft.getDeveloper();
        AppUserObject endUser = releaseDraft.getEndUser();
        ReleaseDraftSingleton.RandomCoreComponentWithStateContainer developerCoreComponentWithStateContainer = releaseDraft.getDeveloperCoreComponentWithStateContainer();
        String existingReleaseNum = releaseDraft.getExistingReleaseNum();
        String newReleaseNum = releaseDraft.getNewReleaseNum();
        CodeListObject codeListCandidate = releaseDraft.getCodeListCandidate();

        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(newReleaseNum);
        ACCObject candidateACC = developerCoreComponentWithStateContainer.getStateACCs().get("Candidate");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(candidateACC.getDen(), existingReleaseNum);
        WebElement asccpNode = accViewEditPage.getNodeByPath("/" + candidateACC.getDen() + "/Adjusted Total Tax Amount");
        assertTrue(asccpNode.isDisplayed());

        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        viewEditCodeListPage.setBranch(existingReleaseNum);
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeListCandidate.getName(), existingReleaseNum);
        assertEquals("99", getText(editCodeListPage.getVersionField()));
        assertEquals("random code list in candidate state", getText(editCodeListPage.getDefinitionField()));
    }

    /**
     * We cannot change BDT now
     */
    @Test
    public void test_TA_18_1_5_d() {

    }

    /**
     * Position of an association changes.
     */
    @Test
    public void test_TA_18_1_5_e() {

    }

}
