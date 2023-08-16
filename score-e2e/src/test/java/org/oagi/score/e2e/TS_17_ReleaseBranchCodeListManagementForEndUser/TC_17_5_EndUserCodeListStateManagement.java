package org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
public class TC_17_5_EndUserCodeListStateManagement extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_17_5_TA_1")
    public void test_TA_1() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        editCodeListPage.setDefinition("new definition");
        editCodeListPage.setDefinitionSource("new definition source");
        assertThrows(TimeoutException.class, () -> {
            editCodeListPage.moveToQA();
        });
    }

    @Test
    @DisplayName("TC_17_5_TA_2")
    public void test_TA_2() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        editCodeListPage.setDefinition("new definition");
        editCodeListPage.setDefinitionSource("new definition source");
        editCodeListPage.hitUpdateButton();
        /**
         * Test Assertion #17.5.2.a
         */
        assertDoesNotThrow(() -> {
            editCodeListPage.moveToQA();
        });
        /**
         * Test Assertion #17.5.2.b  and  Test Assertion #17.5.2.e
         */
        assertDoesNotThrow(() -> {
            editCodeListPage.backToWIP();
        });
        assertThrows(TimeoutException.class, () -> {
            editCodeListPage.moveToProduction();
        });
        /**
         * Test Assertion #17.5.2.c
         */
        editCodeListPage.moveToQA();
        assertDoesNotThrow(() -> {
            editCodeListPage.moveToProduction();
        });
        /**
         * Test Assertion #17.5.2.d
         */
        assertThrows(TimeoutException.class, () -> {
            editCodeListPage.backToWIP();
        });
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
