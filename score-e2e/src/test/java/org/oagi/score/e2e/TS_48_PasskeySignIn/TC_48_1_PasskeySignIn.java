package org.oagi.score.e2e.TS_48_PasskeySignIn;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.virtualauthenticator.HasVirtualAuthenticator;
import org.openqa.selenium.virtualauthenticator.VirtualAuthenticator;
import org.openqa.selenium.virtualauthenticator.VirtualAuthenticatorOptions;

import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Passkey (WebAuthn) sign-in, driven in a real browser with a virtual authenticator that behaves like a platform
 * authenticator with a screen lock (a discoverable credential, user verification succeeding).
 * <p>
 * Passkey sign-in is configured with {@code score.passkey.*} (application.yml or the {@code SCORE_PASSKEY_*}
 * environment variables) and is on exactly when an RP ID and an allowed origin are set. These tests need an instance
 * started that way for the origin in {@code score-e2e.properties}, for example
 * {@code SCORE_PASSKEY_RP_ID=localhost SCORE_PASSKEY_ALLOWED_ORIGINS=http://localhost:4200}, with
 * {@code org.oagi.score.e2e.passkeys.enabled = true} in {@code score-e2e.properties} so that the shared sign-in helper
 * answers the passkey offer that follows a password sign-in. Against an instance without it they are skipped, and
 * only the "nothing is offered" test runs.
 */
public class TC_48_1_PasskeySignIn extends BaseTest {

    private static final By PASSKEY_BUTTON = By.cssSelector(".passkey-login-button");
    private static final By OR_SEPARATOR = By.cssSelector(".or-container");
    private static final By ADD_PASSKEY_BUTTON = By.cssSelector(".btn-add-passkey");
    private static final By PASSKEY_ROW = By.cssSelector(".passkeys-section .passkey-row");
    private static final By LOGIN_ALERT = By.cssSelector(".auth-form .alert");
    private static final By OFFER_DIALOG = By.cssSelector(".passkey-offer-dialog");
    private static final By OFFER_CREATE = By.cssSelector(".passkey-offer-create");
    private static final By OFFER_LATER = By.cssSelector(".passkey-offer-later");
    private static final By OFFER_DONE = By.cssSelector(".passkey-offer-done");

    private AppUserObject appUser;
    private WebDriver driver;
    private VirtualAuthenticator authenticator;

    @BeforeEach
    public void init() {
        super.init();

        // The virtual authenticator needs the augmented driver.
        driver = new Augmenter().augment(getDriver());
        authenticator = ((HasVirtualAuthenticator) driver).addVirtualAuthenticator(new VirtualAuthenticatorOptions()
                .setProtocol(VirtualAuthenticatorOptions.Protocol.CTAP2)
                .setTransport(VirtualAuthenticatorOptions.Transport.INTERNAL)
                .setHasResidentKey(true)
                .setHasUserVerification(true)
                .setIsUserVerified(true));

        appUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
    }

    @AfterEach
    public void cleanUp() {
        getAPIFactory().getApplicationSettingsAPI().deletePasskeysOfLoginId(appUser.getLoginId());
        getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(appUser.getLoginId());
    }

    /**
     * Whether the instance under test has passkey sign-in configured. It reports this without signing in; the
     * browser asks, so the probe reaches the instance the same way the tests do.
     */
    private boolean instanceHasPasskeysConfigured() {
        driver.get(getConfig().getBaseUrl().resolve("/api/info/passkey").toString());
        String body = wait(20).until(ExpectedConditions.presenceOfElementLocated(By.tagName("body"))).getText();
        return body.replace(" ", "").contains("\"enabled\":true");
    }

    private void requirePasskeysConfigured() {
        Assumptions.assumeTrue(instanceHasPasskeysConfigured(),
                "Start the instance with SCORE_PASSKEY_RP_ID and SCORE_PASSKEY_ALLOWED_ORIGINS to run this test.");
        Assumptions.assumeTrue(getConfig().getBooleanProperty("org.oagi.score.e2e.passkeys.enabled"),
                "Set org.oagi.score.e2e.passkeys.enabled = true in score-e2e.properties to run this test.");
    }

    /**
     * Signs in with the password like {@code loginPage().signIn()}, but leaves the passkey offer for the test to
     * answer instead of dismissing it, and does not touch the home page.
     */
    private void signInWithPasswordLeavingTheOffer() {
        // Once the account has a passkey, the login page's silent autofill request would be answered at once by
        // the virtual authenticator and sign the user in before the password is typed. A real browser waits for
        // the user, so make the authenticator refuse while the page loads.
        authenticator.setUserVerified(false);
        loginPage().openPage();
        loginPage().setUsername(appUser.getLoginId());
        loginPage().setPassword(appUser.getPassword());
        loginPage().clickSignIn();
        wait(30).until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        authenticator.setUserVerified(true);
    }

    private void waitForTheOffer() {
        wait(30).until(ExpectedConditions.visibilityOfElementLocated(OFFER_DIALOG));
    }

    /** True if the offer does not appear: it opens a moment after the page does, so give it time. */
    private boolean noOfferAppears() {
        try {
            wait(6).until(ExpectedConditions.visibilityOfElementLocated(OFFER_DIALOG));
            return false;
        } catch (org.openqa.selenium.TimeoutException e) {
            return true;
        }
    }

    private WebDriverWait wait(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    private void openLoginPage() {
        driver.get(getConfig().getBaseUrl().resolve("/login").toString());
        waitForLoginPage();
    }

    /**
     * Signs out and waits for the login page.
     * <p>
     * The login page silently asks the browser for a passkey so that it can be offered in the username field's
     * autofill. A real browser waits for the user to pick one, but a virtual authenticator answers at once and
     * would sign the user straight back in. The server requires user verification, so the authenticator is made to
     * fail it while the page loads (the silent request is refused, as a real browser would leave it unanswered), and
     * is made to succeed again before the test clicks the passkey button.
     */
    private void logoutAndWaitForLoginPage() {
        authenticator.setUserVerified(false);
        driver.get(getConfig().getBaseUrl().resolve("/logout").toString());
        waitForLoginPage();
        authenticator.setUserVerified(true);
    }

    /** Signing out lands on the login page; do not navigate meanwhile, or the sign-out may be cut short. */
    private void waitForLoginPage() {
        wait(30).until(ExpectedConditions.visibilityOfElementLocated(By.id("login_field")));
    }

    private void addPasskeyInAccountSettings(HomePage homePage) {
        homePage.getLoginIDMenu().openSettingsSubMenu().openPasskeysPage();
        WebElement add = wait(20).until(ExpectedConditions.elementToBeClickable(ADD_PASSKEY_BUTTON));
        add.click();
        wait(20).until(ExpectedConditions.numberOfElementsToBe(PASSKEY_ROW, 1));
    }

    private String signedInAuthenticationMethod() {
        String stored = (String) ((JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('X-Score-UserInfo');");
        String json = new String(Base64.getDecoder().decode(stored));
        return json.replaceAll(".*\"authentication\"\\s*:\\s*\"([^\"]*)\".*", "$1");
    }

    @Test
    @DisplayName("TC_48_1_TA_1")
    public void user_registers_a_passkey_and_signs_in_with_it() {
        requirePasskeysConfigured();
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        addPasskeyInAccountSettings(homePage);

        logoutAndWaitForLoginPage();
        wait(20).until(ExpectedConditions.elementToBeClickable(PASSKEY_BUTTON)).click();

        wait(30).until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        assertEquals("passkey", signedInAuthenticationMethod());
        assertTrue(new HomePageProbe(driver).showsLoginId(appUser.getLoginId()),
                "The signed-in account should be the one that registered the passkey");
    }

    @Test
    @DisplayName("TC_48_1_TA_2")
    public void passkey_session_can_still_use_the_application() {
        requirePasskeysConfigured();
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        addPasskeyInAccountSettings(homePage);
        logoutAndWaitForLoginPage();
        wait(20).until(ExpectedConditions.elementToBeClickable(PASSKEY_BUTTON)).click();
        wait(30).until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));

        // A signed-in local account keeps its password form, so the Change password section is still offered.
        driver.get(getConfig().getBaseUrl().resolve("/settings/account/password").toString());
        wait(20).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".btn-update-password")));
        driver.get(getConfig().getBaseUrl().resolve("/settings/account/passkeys").toString());
        wait(20).until(ExpectedConditions.numberOfElementsToBe(PASSKEY_ROW, 1));
    }

    @Test
    @DisplayName("TC_48_1_TA_3")
    public void removed_passkey_can_no_longer_sign_in() {
        requirePasskeysConfigured();
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        addPasskeyInAccountSettings(homePage);

        // Revoke it on the server. The authenticator still holds the passkey.
        getAPIFactory().getApplicationSettingsAPI().deletePasskeysOfLoginId(appUser.getLoginId());

        logoutAndWaitForLoginPage();
        wait(20).until(ExpectedConditions.elementToBeClickable(PASSKEY_BUTTON)).click();

        WebElement alert = wait(20).until(ExpectedConditions.visibilityOfElementLocated(LOGIN_ALERT));
        assertTrue(alert.getText().contains("passkey"), alert.getText());
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @DisplayName("TC_48_1_TA_4")
    public void login_page_has_no_passkey_option_when_it_is_not_configured() {
        Assumptions.assumeFalse(instanceHasPasskeysConfigured(),
                "This instance has passkey sign-in configured; run against one without SCORE_PASSKEY_*.");

        openLoginPage();
        // Give the page time to ask the server; the option must stay absent.
        assertThrows(org.openqa.selenium.TimeoutException.class, () -> wait(3)
                .until(ExpectedConditions.presenceOfElementLocated(PASSKEY_BUTTON)));
        assertTrue(driver.findElements(OR_SEPARATOR).isEmpty());
        assertEquals("username", driver.findElement(By.id("login_field")).getAttribute("autocomplete"));
    }

    @Test
    @DisplayName("TC_48_1_TA_5")
    public void password_sign_in_is_unaffected_by_passkeys() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        assertTrue(homePage.getLoginIDMenu().getLoginIDMenuButton().getText().contains(appUser.getLoginId()));
        assertEquals("basic", signedInAuthenticationMethod());
    }

    @Test
    @DisplayName("TC_48_1_TA_6")
    public void user_creates_a_passkey_from_the_offer_after_signing_in_and_then_signs_in_with_it() {
        requirePasskeysConfigured();

        signInWithPasswordLeavingTheOffer();
        waitForTheOffer();
        wait(20).until(ExpectedConditions.elementToBeClickable(OFFER_CREATE)).click();
        wait(30).until(ExpectedConditions.elementToBeClickable(OFFER_DONE)).click();
        wait(10).until(ExpectedConditions.invisibilityOfElementLocated(OFFER_DIALOG));

        logoutAndWaitForLoginPage();
        wait(20).until(ExpectedConditions.elementToBeClickable(PASSKEY_BUTTON)).click();

        wait(30).until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        assertEquals("passkey", signedInAuthenticationMethod());
    }

    @Test
    @DisplayName("TC_48_1_TA_7")
    public void not_now_keeps_the_offer_away_on_the_next_password_sign_in() {
        requirePasskeysConfigured();

        signInWithPasswordLeavingTheOffer();
        waitForTheOffer();
        wait(20).until(ExpectedConditions.elementToBeClickable(OFFER_LATER)).click();
        wait(10).until(ExpectedConditions.invisibilityOfElementLocated(OFFER_DIALOG));

        logoutAndWaitForLoginPage();
        signInWithPasswordLeavingTheOffer();

        assertTrue(noOfferAppears(), "The offer should stay away for a while after \"Not now\"");
    }

    @Test
    @DisplayName("TC_48_1_TA_8")
    public void no_offer_once_the_account_has_a_passkey() {
        requirePasskeysConfigured();

        signInWithPasswordLeavingTheOffer();
        waitForTheOffer();
        wait(20).until(ExpectedConditions.elementToBeClickable(OFFER_CREATE)).click();
        wait(30).until(ExpectedConditions.elementToBeClickable(OFFER_DONE)).click();
        wait(10).until(ExpectedConditions.invisibilityOfElementLocated(OFFER_DIALOG));

        logoutAndWaitForLoginPage();
        signInWithPasswordLeavingTheOffer();

        assertTrue(noOfferAppears(), "An account that has a passkey should not be offered another");
    }

    /** Reads the signed-in account from the menu in the page header. */
    private static class HomePageProbe {
        private final WebDriver driver;

        HomePageProbe(WebDriver driver) {
            this.driver = driver;
        }

        boolean showsLoginId(String loginId) {
            return new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(d -> d.getPageSource().contains(loginId));
        }
    }

}
