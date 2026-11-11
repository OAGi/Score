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
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.getText;

/**
 * The developer cancels the revision of a Code List after changing its version and its values: the Code List is as it
 * was. Its Agency ID cannot be changed in a revision, and it is what it was.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_11_9_AgencyIDIsFixedInARevisionAndCancelBringsBackTheRest extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_11_9_TA_1")
    public void the_agency_id_is_fixed_in_a_revision_and_cancelling_brings_back_the_version_and_the_values() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        randomAccounts.add(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developer, namespace, workingBranch, "Published");
        CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        String agencyIDList = getText(editCodeListPage.getAgencyIDListField());
        String agencyIDListValue = getText(editCodeListPage.getAgencyIDListValueField());

        editCodeListPage.hitRevise();
        // the Agency ID cannot be changed in a revision
        assertDisabled(editCodeListPage.getAgencyIDListField());
        assertDisabled(editCodeListPage.getAgencyIDListValueField());
        editCodeListPage.setVersion("new version");
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.hitAddButton();
        editCodeListPage.hitUpdateButton();
        assertEquals(agencyIDList, getText(editCodeListPage.getAgencyIDListField()));

        editCodeListPage.hitCancelButton();

        viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        assertEquals("Published", getText(editCodeListPage.getStateField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
        assertEquals(agencyIDList, getText(editCodeListPage.getAgencyIDListField()), "the Agency ID List is as it was");
        assertEquals(agencyIDListValue, getText(editCodeListPage.getAgencyIDListValueField()), "the Agency ID List Value is as it was");
        assertEquals(codeList.getVersionId(), getText(editCodeListPage.getVersionField()));
        EditCodeListPage page = editCodeListPage;
        assertThrows(TimeoutException.class, () -> page.valueExists("new value code"));
        page.valueExists(value.getValue());
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        randomAccounts.forEach(user -> getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(user.getLoginId()));
    }
}
