package org.oagi.score.e2e.TS_21_ModuleManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.module.EditModuleSetPage;
import org.oagi.score.e2e.page.module.ViewEditModuleSetPage;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_21_1_ManageModuleSet extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_21_1_TA_1")
    public void test_TA_1() {
        AppUserObject developer;

        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        assertDoesNotThrow(() -> viewEditModuleSetPage.getNewModuleSetButton());
    }

    @Test
    @DisplayName("TC_21_1_TA_2")
    public void test_TA_2() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        EditModuleSetPage editModuleSetPage =  viewEditModuleSetPage.hitNewModuleSetButton();
        /**
         * Test Assertion #21.1.2
         */
        assertEquals("true", editModuleSetPage.getNameField().getAttribute("aria-required"));
        assertEquals("false", editModuleSetPage.getDescriptionField().getAttribute("aria-required"));
        editModuleSetPage.setName("new module");
        editModuleSetPage.setDescription("Description");
        /**
         * Test Assertion #21.1.2.a
         */
        editModuleSetPage.toggleCreateModuleSetRelease();
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
        editModuleSetPage.setRelease(release.getReleaseNumber());
        editModuleSetPage.setModuleSetRelease("connectSpec 10.9 Module Set Release");
        editModuleSetPage.hitCreateButton();
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
