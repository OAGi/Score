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
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_17_4_AmendAnEndUserCodeList extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_17_4_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListValueMap = new HashMap<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEUA = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            /**
             * Create Production end-user Code List for a particular release branch.
             */
            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespaceEUB, branch, "Production");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserB);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, namespaceEUA, branch, "Production");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserA);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting){
            CodeListValueObject value = codeListValueMap.get(cl);
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), branch.getReleaseNumber());
            int previousRevisionNumber = Integer.parseInt(getText(editCodeListPage.getRevisionField()));
            editCodeListPage.hitAmendButton();
            assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
            assertEquals(previousRevisionNumber+1,Integer.parseInt(getText(editCodeListPage.getRevisionField())));
            assertTrue(getText(editCodeListPage.getCodeListNameField()).equals(cl.getName()));
            assertTrue(getText(editCodeListPage.getDefinitionField()).equals(cl.getDefinition()));
            assertTrue(getText(editCodeListPage.getDefinitionSourceField()).equals(cl.getDefinitionSource()));
            assertTrue(getText(editCodeListPage.getListIDField()).equals(cl.getListId()));
            assertTrue(getText(editCodeListPage.getRemarkField()).equals(cl.getRemark()));
            boolean deprecated;
            if (editCodeListPage.getDeprecatedSelectField().isSelected()){
                deprecated = true;
            }else {
                deprecated = false;
            }
            assertEquals(cl.isDeprecated(), deprecated);

            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(value.getValue());
            assertTrue(getText(editCodeListValueDialog.getCodeField()).equals(value.getValue()));
            assertTrue(getText(editCodeListValueDialog.getMeaningField()).equals(value.getMeaning()));
            assertTrue(getText(editCodeListValueDialog.getDefinitionField()).equals(value.getDefinition()));
            assertTrue(getText(editCodeListValueDialog.getDefinitionSourceField()).equals(value.getDefinitionSource()));

            if (editCodeListValueDialog.getDeprecatedSelectField().isSelected()){
                deprecated = true;
            }else {
                deprecated = false;
            }
            assertEquals(value.isDeprecated(), deprecated);
            escape(getDriver());
        }
    }

    @Test
    @DisplayName("TC_17_4_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListValueMap = new HashMap<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * Create Published developer Code List for a particular release branch.
             */
            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developer, namespace, branch, "Published");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developer);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting){
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), branch.getReleaseNumber());
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(cl.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            assertThrows(TimeoutException.class, () -> {editCodeListPage.hitAmendButton();});

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
