package org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.AssertionHelper;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;

@Execution(ExecutionMode.CONCURRENT)
public class TC_11_4_CreatingANewRevisionOfADeveloperCodeList extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_11_2_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "Published");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);

            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Published");
            codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for(CodeListObject codeList : codeListForTesting){
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            editCodeListPage.hitRevise();
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertEquals("2", getText(editCodeListPage.getRevisionField()));
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            assertEquals(codeList.getName(), getText(editCodeListPage.getCodeListNameField()));
            assertEquals(codeList.getGuid(), getText(editCodeListPage.getGuidField()));
            assertEquals(codeList.getListId(), getText(editCodeListPage.getListIDField()));
            assertEquals(codeList.getDefinition(), getText(editCodeListPage.getDefinitionField()));
            assertEquals(codeList.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
            assertEquals(codeList.getVersionId() + "_New", getText(editCodeListPage.getVersionField()));
            CodeListValueObject value = codeListCodeListValueMap.get(codeList);
            assertDoesNotThrow(() -> editCodeListPage.selectCodeListValue(value.getValue()));
        }

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
