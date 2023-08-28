package org.oagi.score.e2e.TS_27_OAGISDeveloperVisitAboutPage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.help.AboutPage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_27_1_AboutPageContainsFollowingInformation extends BaseTest {

    private AppUserObject appUser;

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

        // Create random end-user
        appUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_27_1_TA_1abcd")
    public void the_version_of_the_application_in_the_version_field() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        AboutPage aboutPage = homePage.getHelpMenu().openAboutSubMenu();
        AboutPage.VersionList versionList = aboutPage.getVersionList();

        String regEx = "[\\d+\\.].*";
        assertTrue(getText(versionList.getItemByName("score-web")).matches("score-web : " + regEx));
        assertTrue(getText(versionList.getItemByName("score-http")).matches("score-http : " + regEx));
        assertTrue(getText(versionList.getItemByName("MariaDB")).matches("MariaDB : " + regEx));
        assertTrue(getText(versionList.getItemByName("Redis")).matches("Redis : " + regEx));
    }

    @Test
    @DisplayName("TC_27_1_TA_1e")
    public void link_to_the_contributors_of_the_application() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        AboutPage aboutPage = homePage.getHelpMenu().openAboutSubMenu();
        assertTrue(aboutPage.getContributorsLink().isDisplayed());
    }

    @Test
    @DisplayName("TC_27_1_TA_1f")
    public void the_license_of_the_application() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        AboutPage aboutPage = homePage.getHelpMenu().openAboutSubMenu();
        assertTrue(aboutPage.getLicense().isDisplayed());
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

}
