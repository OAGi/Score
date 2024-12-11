package org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper;

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
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getText;

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
    @DisplayName("TC_11_4_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        LibraryObject library;
        ReleaseObject workingBranch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<BigInteger, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "Published");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListCodeListValueMap.put(codeList.getCodeListManifestId(), codeListValue);
            codeListForTesting.add(codeList);

            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Published");
            codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList.getCodeListManifestId(), codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
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
            CodeListValueObject value = codeListCodeListValueMap.get(codeList.getCodeListManifestId());

            WebElement tr = editCodeListPage.getTableRecordByValue(value.getValue());
            WebElement td = editCodeListPage.getColumnByName(tr, "value");
            assertEquals(value.getValue(), getText(td));

            td = editCodeListPage.getColumnByName(tr, "meaning");
            assertEquals(value.getMeaning(), getText(td));
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
