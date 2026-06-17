package org.oagi.score.e2e.impl.page.oas;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.oas.OasSecuritySchemeDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.clear;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.escape;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.isElementPresent;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfAllElementsLocatedBy;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/**
 * Implementation of {@link OasSecuritySchemeDialog} (Issue #1729).
 */
public class OasSecuritySchemeDialogImpl implements OasSecuritySchemeDialog {

    private static final String BASE_XPATH = "//mat-dialog-container";
    private static final String FLOW_CARD_XPATH = BASE_XPATH + "//div[contains(@class, \"oas-flow-card\")]";

    private static final By TITLE_LOCATOR =
            By.xpath(BASE_XPATH + "//*[contains(@class, \"mat-mdc-dialog-title\")]");
    private static final By TYPE_SELECT_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"Type\"]/ancestor::mat-form-field[1]//mat-select");
    private static final By SCHEME_NAME_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"Scheme Name\"]/ancestor::mat-form-field[1]//input");
    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"Description (optional)\"]/ancestor::mat-form-field[1]//input");
    private static final By IN_SELECT_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"In\"]/ancestor::mat-form-field[1]//mat-select");
    private static final By API_KEY_NAME_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"Name\"]/ancestor::mat-form-field[1]//input");
    private static final By HTTP_SCHEME_SELECT_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"Scheme\"]/ancestor::mat-form-field[1]//mat-select");
    private static final By BEARER_FORMAT_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"Bearer Format\"]/ancestor::mat-form-field[1]//input");
    private static final By OPEN_ID_CONNECT_URL_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"OpenID Connect URL\"]/ancestor::mat-form-field[1]//input");
    private static final By OAUTH_FLOWS_SECTION_LOCATOR =
            By.xpath(BASE_XPATH + "//span[normalize-space(.) = \"OAuth Flows\"]");
    private static final By ADD_FLOW_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//span[normalize-space(.) = \"OAuth Flows\"]/following-sibling::button[1]");
    private static final By FLOW_CARD_LOCATOR = By.xpath(FLOW_CARD_XPATH);
    private static final By PRIMARY_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[normalize-space(.) = \"Add\" or normalize-space(.) = \"Save\"]");
    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[normalize-space(.) = \"Cancel\"]");

    private final BasePageImpl parent;

    public OasSecuritySchemeDialogImpl(BasePageImpl parent) {
        this.parent = parent;
    }

    private WebDriver getDriver() {
        return this.parent.getDriver();
    }

    @Override
    public boolean isOpened() {
        try {
            getTitle();
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
    }

    private String valueOf(By locator) {
        String value = visibilityOfElementLocated(getDriver(), locator).getAttribute("value");
        return value == null ? "" : value;
    }

    private void selectOption(By selectLocator, String optionLabel) {
        click(getDriver(), visibilityOfElementLocated(getDriver(), selectLocator));
        WebElement option = elementToBeClickable(getDriver(),
                By.xpath("//mat-option[normalize-space(.) = \"" + optionLabel + "\"]"));
        click(getDriver(), option);
        waitFor(ofMillis(400L));
    }

    private List<String> optionsOf(By selectLocator) {
        click(getDriver(), visibilityOfElementLocated(getDriver(), selectLocator));
        List<String> options = visibilityOfAllElementsLocatedBy(getDriver(), By.xpath("//mat-option"))
                .stream()
                .map(option -> getText(option).trim())
                .collect(Collectors.toList());
        escape(getDriver());
        waitFor(ofMillis(300L));
        return options;
    }

    @Override
    public void setType(String typeLabel) {
        selectOption(TYPE_SELECT_LOCATOR, typeLabel);
    }

    @Override
    public String getType() {
        return getText(visibilityOfElementLocated(getDriver(), TYPE_SELECT_LOCATOR));
    }

    @Override
    public List<String> getTypeOptions() {
        return optionsOf(TYPE_SELECT_LOCATOR);
    }

    @Override
    public String getSchemeName() {
        return valueOf(SCHEME_NAME_FIELD_LOCATOR);
    }

    @Override
    public void setSchemeName(String schemeName) {
        sendKeys(visibilityOfElementLocated(getDriver(), SCHEME_NAME_FIELD_LOCATOR), schemeName);
    }

    @Override
    public void setDescription(String description) {
        sendKeys(visibilityOfElementLocated(getDriver(), DESCRIPTION_FIELD_LOCATOR), description);
    }

    @Override
    public boolean isInFieldDisplayed() {
        return isElementPresent(getDriver(), IN_SELECT_LOCATOR);
    }

    @Override
    public List<String> getInOptions() {
        return optionsOf(IN_SELECT_LOCATOR);
    }

    @Override
    public void setIn(String inLabel) {
        selectOption(IN_SELECT_LOCATOR, inLabel);
    }

    @Override
    public String getApiKeyName() {
        return valueOf(API_KEY_NAME_FIELD_LOCATOR);
    }

    @Override
    public void setApiKeyName(String name) {
        sendKeys(visibilityOfElementLocated(getDriver(), API_KEY_NAME_FIELD_LOCATOR), name);
    }

    @Override
    public void clearApiKeyName() {
        clear(visibilityOfElementLocated(getDriver(), API_KEY_NAME_FIELD_LOCATOR));
        waitFor(ofMillis(300L));
    }

    @Override
    public void setHttpScheme(String schemeLabel) {
        selectOption(HTTP_SCHEME_SELECT_LOCATOR, schemeLabel);
    }

    @Override
    public boolean isBearerFormatFieldDisplayed() {
        return isElementPresent(getDriver(), BEARER_FORMAT_FIELD_LOCATOR);
    }

    @Override
    public boolean isOpenIdConnectUrlFieldDisplayed() {
        return isElementPresent(getDriver(), OPEN_ID_CONNECT_URL_FIELD_LOCATOR);
    }

    @Override
    public String getOpenIdConnectUrl() {
        return valueOf(OPEN_ID_CONNECT_URL_FIELD_LOCATOR);
    }

    @Override
    public void setOpenIdConnectUrl(String url) {
        sendKeys(visibilityOfElementLocated(getDriver(), OPEN_ID_CONNECT_URL_FIELD_LOCATOR), url);
    }

    @Override
    public void clearOpenIdConnectUrl() {
        clear(visibilityOfElementLocated(getDriver(), OPEN_ID_CONNECT_URL_FIELD_LOCATOR));
        waitFor(ofMillis(300L));
    }

    @Override
    public boolean isOAuthFlowsSectionDisplayed() {
        return isElementPresent(getDriver(), OAUTH_FLOWS_SECTION_LOCATOR);
    }

    @Override
    public int getFlowCount() {
        return getDriver().findElements(FLOW_CARD_LOCATOR).size();
    }

    private String flowCard(int flowIndex) {
        return "(" + FLOW_CARD_XPATH + ")[" + flowIndex + "]";
    }

    @Override
    public String getFlowType(int flowIndex) {
        return getText(visibilityOfElementLocated(getDriver(), By.xpath(
                flowCard(flowIndex) + "//mat-label[normalize-space(.) = \"Flow Type\"]/ancestor::mat-form-field[1]//mat-select")));
    }

    @Override
    public boolean isFlowAuthorizationUrlDisplayed(int flowIndex) {
        return isElementPresent(getDriver(), By.xpath(
                flowCard(flowIndex) + "//mat-label[normalize-space(.) = \"Authorization URL\"]"));
    }

    @Override
    public boolean isFlowTokenUrlDisplayed(int flowIndex) {
        return isElementPresent(getDriver(), By.xpath(
                flowCard(flowIndex) + "//mat-label[normalize-space(.) = \"Token URL\"]"));
    }

    @Override
    public List<String> getFlowScopeNames(int flowIndex) {
        return getDriver().findElements(By.xpath(
                        flowCard(flowIndex) + "//mat-label[normalize-space(.) = \"Scope\"]/ancestor::mat-form-field[1]//input"))
                .stream()
                .map(input -> {
                    String value = input.getAttribute("value");
                    return value == null ? "" : value;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void addFlow() {
        click(getDriver(), elementToBeClickable(getDriver(), ADD_FLOW_BUTTON_LOCATOR));
        waitFor(ofMillis(400L));
    }

    @Override
    public void addScope(int flowIndex) {
        click(getDriver(), elementToBeClickable(getDriver(), By.xpath(
                flowCard(flowIndex) + "//span[normalize-space(.) = \"Scopes\"]/following-sibling::button[1]")));
        waitFor(ofMillis(400L));
    }

    @Override
    public void removeLastScope(int flowIndex) {
        List<WebElement> removeButtons = getDriver().findElements(By.xpath(
                flowCard(flowIndex) + "//button[.//mat-icon[normalize-space(.) = \"remove\"]]"));
        // The first 'remove' button is 'Remove Flow'; subsequent ones are per-scope 'Remove Scope'.
        click(getDriver(), removeButtons.get(removeButtons.size() - 1));
        waitFor(ofMillis(400L));
    }

    @Override
    public boolean isPrimaryButtonEnabled() {
        WebElement button = visibilityOfElementLocated(getDriver(), PRIMARY_BUTTON_LOCATOR);
        String klass = button.getAttribute("class");
        return button.isEnabled() && (klass == null || !klass.contains("mat-mdc-button-disabled"));
    }

    @Override
    public void hitPrimaryButton() {
        click(elementToBeClickable(getDriver(), PRIMARY_BUTTON_LOCATOR));
        invisibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
        waitFor(ofMillis(500L));
    }

    @Override
    public void cancel() {
        click(elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR));
        waitFor(ofMillis(500L));
    }
}
