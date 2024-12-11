package org.oagi.score.e2e.TS_1_OAGISDeveloperAuthenticationAndAuthorizedFunctions;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.SignInException;
import org.oagi.score.e2e.page.HomePage;
import org.openqa.selenium.WebElement;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBar;

@Execution(ExecutionMode.CONCURRENT)
public class TC_1_1_BuiltInOAGIDeveloperAccountExists extends BaseTest {

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_1_TA_1")
    public void test_login_oagis() {
        loginPage().signIn("oagis", "oagis");
    }

    @Test
    @DisplayName("TC_1_TA_2")
    public void verify_oagis_role() {
        HomePage homePage = loginPage().signIn("oagis", "oagis");
        String role = "developer";
        assertTrue(getSnackBar(getDriver(), role).isDisplayed());

        // Issue #1275
        WebElement loginIDMenu = homePage.getLoginIDMenu().getLoginIDMenuButton();
        assertTrue(loginIDMenu.getText().contains("(" + role + ")"));
    }

    @Test
    @DisplayName("TC_1_TA_3")
    public void test_logout() throws URISyntaxException {
        HomePage homePage = loginPage().signIn("oagis", "oagis");
        homePage.getLoginIDMenu().logout();

        URI baseURL = this.getConfig().getBaseUrl().resolve("/");
        URI currentURL = new URI(getDriver().getCurrentUrl()).resolve("/");

        assertTrue(baseURL.equals(currentURL));
    }

    @Test
    @DisplayName("TC_1_TA_4")
    public void test_login_oagis_with_wrong_password() {
        SignInException exception = assertThrows(SignInException.class, () -> {
            loginPage().signIn("oagis", RandomStringUtils.secure().nextAlphanumeric(5, 10));
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    public void test_login_random_username() {
        SignInException exception = assertThrows(SignInException.class, () -> {
            loginPage().signIn(RandomStringUtils.secure().nextAlphanumeric(5, 10), RandomStringUtils.secure().nextAlphanumeric(5, 10));
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

}
