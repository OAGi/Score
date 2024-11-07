package org.oagi.score.e2e.TS_18_DraftReleaseBranchCoreComponentCodeListAccessDevelopersEndUsers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.release.CreateReleasePage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.escape;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_18_2_CodeListAccess extends BaseTest {
    String existingReleaseNum = null;
    String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230519, 20231231)));
    CodeListObject codeListCandidate;
    RandomCodeListWithStateContainer developerCodeListWithStateContainer;
    RandomCodeListWithStateContainer euCodeListWithStateContainer;
    AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
    AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
        if (existingReleaseNum == null) {
            draft_creation();
            existingReleaseNum = newReleaseNum;
        }
    }

    private void draft_creation() {
        ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        ReleaseObject euBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.8");
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        List<String> clStates = new ArrayList<>();
        clStates.add("WIP");
        clStates.add("Draft");
        clStates.add("Candidate");
        clStates.add("Deleted");
        developerCodeListWithStateContainer = new RandomCodeListWithStateContainer(developer, workingBranch, namespace, clStates);

        List<String> euCLStates = new ArrayList<>();
        euCLStates.add("WIP");
        euCLStates.add("QA");
        euCLStates.add("Production");
        euCLStates.add("Deleted");
        euCodeListWithStateContainer = new RandomCodeListWithStateContainer(endUser, euBranch, euNamespace, euCLStates);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.showAdvancedSearchPanel();
        viewEditReleasePage.setState("Draft");
        viewEditReleasePage.hitSearchButton();
        long timeout = Duration.ofSeconds(300L).toMillis();
        long begin = System.currentTimeMillis();
        if (viewEditReleasePage.getTotalNumberOfItems() > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            String oldDraftRelease = getText(editReleasePage.getReleaseNumberField());
            editReleasePage.backToInitialized();
            begin = System.currentTimeMillis();
            while (System.currentTimeMillis() - begin < timeout) {
                viewEditReleasePage.openPage();
                viewEditReleasePage.setReleaseNum(oldDraftRelease);
                viewEditReleasePage.hitSearchButton();
                tr = viewEditReleasePage.getTableRecordAtIndex(1);
                String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
                if ("Initialized".equals(state)) {
                    break;
                }
            }
        }
        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(namespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitCreateButton();

        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        timeout = Duration.ofSeconds(300L).toMillis();
        begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            if ("Draft".equals(state)) {
                break;
            }
        }
        homePage.logout();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    public void test_TA_18_2_1() {
        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        viewEditCodeListPage.setBranch(existingReleaseNum);
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"New Code List\")]//ancestor::button[1]")).size());

        CodeListObject codeListCandidate = developerCodeListWithStateContainer.stateCodeLists.get("Candidate");
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeListCandidate);
        assertDisabled(editCodeListPage.getNamespaceSelectField());
        assertDisabled(editCodeListPage.getListIDField());
        assertDisabled(editCodeListPage.getAgencyIDListField());
        assertDisabled(editCodeListPage.getNamespaceSelectField());
        assertDisabled(editCodeListPage.getDefinitionField());
        assertDisabled(editCodeListPage.getDefinitionSourceField());
        assertDisabled(editCodeListPage.getDeprecatedSelectField());

        //openFirstCodeListValue
        String codeListValue = developerCodeListWithStateContainer.stateCodeListValues.get("Candidate").getValue();
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(codeListValue);
        assertDisabled(editCodeListValueDialog.getCodeField());
        assertDisabled(editCodeListValueDialog.getMeaningField());
        assertDisabled(editCodeListValueDialog.getDefinitionField());
        assertDisabled(editCodeListValueDialog.getDefinitionSourceField());
        assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
        escape(getDriver());
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Revise\")]//ancestor::button[1]")).size());
    }

    @Test
    public void test_TA_18_2_2() {
        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        List<String> clStates = new ArrayList<>();
        clStates.add("WIP");
        clStates.add("QA");
        clStates.add("Production");
        clStates.add("Deleted");
        viewEditCodeListPage.setBranch(existingReleaseNum);
        viewEditCodeListPage.showAdvancedSearchPanel();

        for (String state : clStates) {
            viewEditCodeListPage.setState(state);
            viewEditCodeListPage.hitSearchButton();
            assertEquals(0, getDriver().findElements(By.xpath("//score-cc-list//table//tbody//tr")).size());
        }
    }

    @Test
    public void test_TA_18_2_3() {
        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        viewEditCodeListPage.setBranch(existingReleaseNum);
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"New Code List\")]//ancestor::button[1]")).size());

        CodeListObject codeListCandidate = developerCodeListWithStateContainer.stateCodeLists.get("Candidate");
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeListCandidate);
        assertDisabled(editCodeListPage.getNamespaceSelectField());
        assertDisabled(editCodeListPage.getListIDField());
        assertDisabled(editCodeListPage.getAgencyIDListField());
        assertDisabled(editCodeListPage.getNamespaceSelectField());
        assertDisabled(editCodeListPage.getDefinitionField());
        assertDisabled(editCodeListPage.getDefinitionSourceField());
        assertDisabled(editCodeListPage.getDeprecatedSelectField());

        //openFirstCodeListValue
        String codeListValue = developerCodeListWithStateContainer.stateCodeListValues.get("Candidate").getValue();
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(codeListValue);
        assertDisabled(editCodeListValueDialog.getCodeField());
        assertDisabled(editCodeListValueDialog.getMeaningField());
        assertDisabled(editCodeListValueDialog.getDefinitionField());
        assertDisabled(editCodeListValueDialog.getDefinitionSourceField());
        assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
        escape(getDriver());
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Derive Code List based on this\")]//ancestor::button[1]")).size());
    }

    private class RandomCodeListWithStateContainer {
        private final AppUserObject appUser;
        private List<String> states = new ArrayList<>();
        private final HashMap<String, CodeListObject> stateCodeLists = new HashMap<>();
        private final HashMap<String, CodeListValueObject> stateCodeListValues = new HashMap<>();

        public RandomCodeListWithStateContainer(AppUserObject appUser, ReleaseObject release, NamespaceObject namespace, List<String> states) {
            this.appUser = appUser;
            this.states = states;

            for (int i = 0; i < this.states.size(); ++i) {
                CodeListObject codeList;
                CodeListValueObject codeListValue;
                String state = this.states.get(i);
                {
                    codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(this.appUser, namespace, release, state);
                    codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, this.appUser);
                    stateCodeLists.put(state, codeList);
                    stateCodeListValues.put(state, codeListValue);
                }
            }
        }

    }

}
