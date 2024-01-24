package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.SignInException;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
import org.openqa.selenium.*;

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
        assert "Sign in to NIST/OAGi Score".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.tagName("h1"));
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
        By locator = By.cssSelector("input[value=\"Sign in\"]");
        return elementToBeClickable(getDriver(), locator);
    }

    @Override
    public void setUsername(String username) {
        sendKeys(getUsernameInput(), username);
    }

    @Override
    public void setPassword(String password) {
        sendKeys(getPasswordInput(), password);
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
            assert homePage.getScoreLogo().isDisplayed();
        } catch (TimeoutException e) {
            try {
                WebElement redirectedPageTitle = getDriver().findElement(By.cssSelector(".mat-mdc-card-title"));
                throw new SignInException(getText(redirectedPageTitle), e);
            } catch (NoSuchElementException ignore) {
                throw new SignInException(getAlertMessage(), e);
            }
        }
        return homePage;
    }
}
