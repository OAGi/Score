package org.oagi.score.e2e.TS_33_CodeListUplifting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Release;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.UpliftCodeListPage;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.oagi.score.e2e.impl.PageHelper.escape;

@Execution(ExecutionMode.CONCURRENT)
public class TC_33_2_EndUserCodeListUplifting extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_33_2_TA_1")
    public void test_TA_1() {
        AppUserObject endUser;
        CodeListObject codeList;
        ReleaseObject release;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "Published");

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        upliftCodeListPage.setSourceRelease(release.getReleaseNumber());
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        upliftCodeListPage.setTargetRelease(targetRelease.getReleaseNumber());
        upliftCodeListPage.selectCodeList(codeList.getName());

        /**
         * The target release must be greater than the source release.
         */
        List<String> releasesBefore = getAPIFactory().getReleaseAPI().getAllReleasesBeforeRelease(release);
        for (String earlierRelease: releasesBefore){
            assertThrows(WebDriverException.class, () -> upliftCodeListPage.setTargetRelease(earlierRelease));
        }
        assertThrows(WebDriverException.class, () -> upliftCodeListPage.setTargetRelease(release.getReleaseNumber()));

        /**
         * The end user cannot select the Working branch.
         */
        escape(getDriver());
        ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        assertThrows(WebDriverException.class, () -> upliftCodeListPage.setSourceRelease(workingBranch.getReleaseNumber()));
        assertThrows(WebDriverException.class, () -> upliftCodeListPage.setTargetRelease(workingBranch.getReleaseNumber()));
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
