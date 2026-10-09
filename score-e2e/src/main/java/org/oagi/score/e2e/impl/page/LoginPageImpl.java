package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.SignInException;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static java.time.Duration.ofSeconds;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class LoginPageImpl extends BasePageImpl implements LoginPage {

    public LoginPageImpl(BasePage parent) {
        super(parent);
    }

    public LoginPageImpl(WebDriver driver, Configuration config, APIFactory apiFactory) {
        super(driver, config, apiFactory);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/login").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        waitFor(ofSeconds(1L));
        WebElement usernameInput = visibilityOfElementLocated(org.oagi.score.e2e.impl.PageHelper.wait(getDriver(), ofSeconds(10L), ofSeconds(1L)), By.cssSelector("input[id=\"login_field\"]"));
        assert usernameInput.isDisplayed();
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(org.oagi.score.e2e.impl.PageHelper.wait(getDriver(), ofSeconds(10L), ofSeconds(1L)), By.tagName("h1"));
    }

    @Override
    public WebElement getAlert() {
        return visibilityOfElementLocated(getDriver(), By.cssSelector("div[role=\"alert\"]"));
    }

    public String getAlertMessage() {
        String message = getText(getAlert());
        return message.endsWith("×") ? trim(message.substring(0, message.length() - 1)) : message;
    }

    @Override
    public WebElement getUsernameInput() {
        By locator = By.cssSelector("input[id=\"login_field\"]");
        return visibilityOfElementLocated(getDriver(), locator);
    }

    @Override
    public WebElement getPasswordInput() {
        By locator = By.cssSelector("input[id=\"password\"]");
        return visibilityOfElementLocated(getDriver(), locator);
    }

    @Override
    public WebElement getSignInButton() {
        By locator = By.xpath("//button[@type=\"submit\" and contains(normalize-space(.), \"Sign in\")]"
                + " | //input[@value=\"Sign in\"]");
        return elementToBeClickable(getDriver(), locator);
    }

    @Override
    public void setUsername(String username) {
        sendKeys(getUsernameInput(), username);
    }

    @Override
    public void setPassword(String password) {
        revealPasswordField();
        sendKeys(getPasswordInput(), password);
    }

    /**
     * On an instance with passkey sign-in configured, the login page first asks for the username only. Continue
     * shows the password field, or, for an account with a passkey, the passkey prompt; when that prompt cannot be
     * answered the page offers "Try another way", which shows the password field.
     */
    private void revealPasswordField() {
        if (!getConfig().getBooleanProperty("org.oagi.score.e2e.passkeys.enabled")) {
            return;
        }
        By password = By.cssSelector("input[id=\"password\"]");
        if (!getDriver().findElements(password).isEmpty()) {
            return;
        }
        click(getDriver(), elementToBeClickable(getDriver(), By.cssSelector(".passkey-continue-button")));
        By anotherWay = By.cssSelector(".passkey-another-way-button");
        new org.openqa.selenium.support.ui.WebDriverWait(getDriver(), java.time.Duration.ofSeconds(20)).until(
                d -> !d.findElements(password).isEmpty() || !d.findElements(anotherWay).isEmpty());
        if (getDriver().findElements(password).isEmpty()) {
            click(getDriver(), elementToBeClickable(getDriver(), anotherWay));
        }
    }

    @Override
    public void clickSignIn() {
        click(getDriver(), getSignInButton());
    }

    @Override
    public HomePage signIn(String username, String password) throws SignInException {
        this.openPage();

        this.setUsername(username);
        this.setPassword(password);

        this.clickSignIn();

        AppUserObject user = this.getAPIFactory().getAppUserAPI().getAppUserByLoginID(username);
        HomePage homePage = new HomePageImpl(this, user);
        try {
            assert homePage.getConnectCenterLogo().isDisplayed();
        } catch (TimeoutException e) {
            try {
                WebElement redirectedPageTitle = getDriver().findElement(By.cssSelector(".mat-mdc-card-title"));
                throw new SignInException(getText(redirectedPageTitle), e);
            } catch (NoSuchElementException ignore) {
                throw new SignInException(getAlertMessage(), e);
            }
        }

        dismissPasskeyOfferIfEnabled();

        /*
         * To make the 'connectSpec' as a default selected library.
         */
        homePage.setLibrary("connectSpec");

        return homePage;
    }

    /**
     * On an instance with passkey sign-in configured, a user without a passkey is offered one right after signing in
     * with a password, in a dialog that would cover the page. Tests that are not about passkeys answer "Not now".
     * <p>
     * It is only looked for when {@code org.oagi.score.e2e.passkeys.enabled} is {@code true} in
     * {@code score-e2e.properties}, so instances without passkeys pay no waiting time.
     */
    private void dismissPasskeyOfferIfEnabled() {
        if (!getConfig().getBooleanProperty("org.oagi.score.e2e.passkeys.enabled")) {
            return;
        }

        By notNow = By.cssSelector(".passkey-offer-later");
        try {
            new WebDriverWait(getDriver(), ofSeconds(5L)).until(ExpectedConditions.elementToBeClickable(notNow)).click();
            new WebDriverWait(getDriver(), ofSeconds(10L)).until(ExpectedConditions.invisibilityOfElementLocated(notNow));
        } catch (TimeoutException ignore) {
            // No offer: the account already has a passkey, or it was dismissed before.
        }
    }
}
