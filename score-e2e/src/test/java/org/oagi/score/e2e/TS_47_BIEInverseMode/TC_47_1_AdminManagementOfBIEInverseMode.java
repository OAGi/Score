package org.oagi.score.e2e.TS_47_BIEInverseMode;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the global setting. It must not overlap any test that profiles or generates BIEs. */
@Isolated("BIE Inverse Mode changes a global application setting used by other E2E tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
public class TC_47_1_AdminManagementOfBIEInverseMode extends BaseTest {

    private static final By INVERSE_MODE_LABEL = By.xpath("//mat-label[normalize-space(.)='BIE Inverse Mode']");
    private static final By ENABLE_BUTTON = By.xpath("//mat-label[normalize-space(.)='BIE Inverse Mode']/following-sibling::div//button[.//span[normalize-space(.)='Enable']]");
    private static final By DISABLE_BUTTON = By.xpath("//mat-label[normalize-space(.)='BIE Inverse Mode']/following-sibling::div//button[.//span[normalize-space(.)='Disable']]");
    private static final By DIALOG_ENABLE_BUTTON = By.xpath("//mat-dialog-container//button[.//span[normalize-space(.)='Enable']]");
    private static final By DIALOG_DISABLE_BUTTON = By.xpath("//mat-dialog-container//button[.//span[normalize-space(.)='Disable']]");
    private static final By DIALOG_CANCEL_BUTTON = By.xpath("//mat-dialog-container//button[.//span[normalize-space(.)='Cancel']]");

    private boolean originalInverseModeSetting;

    @BeforeAll
    void captureAndResetSetting() {
        originalInverseModeSetting = getAPIFactory().getApplicationSettingsAPI().isBIEInverseModeEnabled();
        getAPIFactory().getApplicationSettingsAPI().setBIEInverseModeEnable(false);
    }

    @BeforeEach
    @Override
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_47_1_TA_1: a non-admin cannot view the global BIE Inverse Mode setting")
    public void non_admin_cannot_open_application_settings_directly() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        try {
            loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
            getDriver().get(getConfig().getBaseUrl().resolve("/settings/application_settings").toString());
            assertFalse(getDriver().findElements(INVERSE_MODE_LABEL).stream().anyMatch(WebElement::isDisplayed),
                    "A non-admin must not see the BIE Inverse Mode application setting on the direct route.");
        } finally {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(endUser.getLoginId());
        }
    }

    @Test
    @DisplayName("TC_47_1_TA_2: enable and disable confirmation can be cancelled or confirmed")
    public void admin_can_toggle_the_setting_and_cancel_without_changing_it() {
        loginPage().signIn("oagis", "oagis");
        getDriver().get(getConfig().getBaseUrl().resolve("/settings/application_settings").toString());
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVERSE_MODE_LABEL));

        clickWhenReady(wait, ENABLE_BUTTON);
        clickWhenReady(wait, DIALOG_CANCEL_BUTTON);
        assertFalse(getAPIFactory().getApplicationSettingsAPI().isBIEInverseModeEnabled(),
                "Cancelling Enable must leave the global setting disabled.");

        clickWhenReady(wait, ENABLE_BUTTON);
        clickWhenReady(wait, DIALOG_ENABLE_BUTTON);
        wait.until(ignored -> getAPIFactory().getApplicationSettingsAPI().isBIEInverseModeEnabled());

        clickWhenReady(wait, DISABLE_BUTTON);
        clickWhenReady(wait, DIALOG_CANCEL_BUTTON);
        assertTrue(getAPIFactory().getApplicationSettingsAPI().isBIEInverseModeEnabled(),
                "Cancelling Disable must leave the global setting enabled.");

        clickWhenReady(wait, DISABLE_BUTTON);
        clickWhenReady(wait, DIALOG_DISABLE_BUTTON);
        wait.until(ignored -> !getAPIFactory().getApplicationSettingsAPI().isBIEInverseModeEnabled());
    }

    private void clickWhenReady(WebDriverWait wait, By locator) {
        wait.until(driver -> {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed() && element.isEnabled()) {
                    element.click();
                    return true;
                }
            } catch (StaleElementReferenceException ignored) {
                // Retry with a fresh reference after Angular replaces the dialog DOM.
            }
            return false;
        });
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @AfterAll
    void restoreOriginalSetting() {
        getAPIFactory().getApplicationSettingsAPI().setBIEInverseModeEnable(originalInverseModeSetting);
    }
}
