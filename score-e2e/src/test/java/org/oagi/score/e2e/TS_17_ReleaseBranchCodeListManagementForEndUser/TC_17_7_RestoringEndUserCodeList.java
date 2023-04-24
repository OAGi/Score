package org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_17_7_RestoringEndUserCodeList extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_17_7_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEUa = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            NamespaceObject namespaceEUb = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUserA, namespaceEUa, branch, "Deleted");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserA);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);

            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUserB, namespaceEUb, branch, "Deleted");
            codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserB);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), branch.getReleaseNumber());
            assertEquals("Deleted", getText(editCodeListPage.getStateField()));
            editCodeListPage.hitRestoreButton();
            CodeListValueObject value = codeListCodeListValueMap.get(codeList);
            assertDoesNotThrow(() -> editCodeListPage.valueExists(value.getValue()));
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            assertEquals(codeList.getVersionId(), getText(editCodeListPage.getVersionField()));
            assertEquals(codeList.getDefinition(), getText(editCodeListPage.getDefinitionField()));
            assertEquals(codeList.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
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
