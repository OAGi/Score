package org.oagi.score.e2e.impl.page.oas;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.oas.OasSecurityRequirementDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.escape;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.isChecked;
import static org.oagi.score.e2e.impl.PageHelper.isElementPresent;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/**
 * Implementation of {@link OasSecurityRequirementDialog} (Issue #1729).
 */
public class OasSecurityRequirementDialogImpl implements OasSecurityRequirementDialog {

    private static final String BASE_XPATH = "//mat-dialog-container";
    // Match the exact 'requirement-card' class token so it does NOT also match 'requirement-card-header'.
    private static final String REQ_CARD_XPATH = BASE_XPATH
            + "//div[contains(concat(\" \", normalize-space(@class), \" \"), \" requirement-card \")]";

    private static final By TITLE_LOCATOR =
            By.xpath(BASE_XPATH + "//*[contains(@class, \"mat-mdc-dialog-title\")]");
    private static final By MODE_RADIO_GROUP_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-radio-group[contains(@class, \"security-mode-group\")]");
    private static final By REQ_CARD_LOCATOR = By.xpath(REQ_CARD_XPATH);
    private static final By DUPLICATE_WARNING_LOCATOR =
            By.xpath(BASE_XPATH + "//div[contains(@class, \"requirement-error\")]");
    private static final By ADD_ALTERNATIVE_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[contains(., \"Add Alternative\")]");
    private static final By APPLY_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[normalize-space(.) = \"Apply\"]");
    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[normalize-space(.) = \"Cancel\"]");

    private final BasePageImpl parent;

    public OasSecurityRequirementDialogImpl(BasePageImpl parent) {
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

    @Override
    public boolean hasModeRadioGroup() {
        return isElementPresent(getDriver(), MODE_RADIO_GROUP_LOCATOR);
    }

    @Override
    public void setMode(String modeLabel) {
        WebElement radio = elementToBeClickable(getDriver(), By.xpath(
                BASE_XPATH + "//mat-radio-button[contains(normalize-space(.), \"" + modeLabel + "\")]"));
        click(getDriver(), radio);
        waitFor(ofMillis(400L));
    }

    @Override
    public int getRequirementCount() {
        return getDriver().findElements(REQ_CARD_LOCATOR).size();
    }

    private String card(int cardIndex) {
        return "(" + REQ_CARD_XPATH + ")[" + cardIndex + "]";
    }

    @Override
    public void setRequirementScheme(int cardIndex, int rowIndex, String schemeName) {
        WebElement select = visibilityOfElementLocated(getDriver(), By.xpath(
                "(" + card(cardIndex) + "//mat-form-field[contains(@class, \"scheme-field\")]//mat-select)[" + rowIndex + "]"));
        click(getDriver(), select);
        WebElement option = elementToBeClickable(getDriver(), By.xpath(
                "//mat-option[starts-with(normalize-space(.), \"" + schemeName + " (\")]"));
        click(getDriver(), option);
        waitFor(ofMillis(400L));
    }

    @Override
    public void setRequirementScopes(int cardIndex, int rowIndex, List<String> scopes) {
        WebElement select = visibilityOfElementLocated(getDriver(), By.xpath(
                "(" + card(cardIndex) + "//div[contains(@class, \"scheme-row\")])[" + rowIndex
                        + "]//mat-form-field[contains(@class, \"scopes-field\")]//mat-select"));
        click(getDriver(), select);
        for (String scope : scopes) {
            WebElement option = elementToBeClickable(getDriver(), By.xpath(
                    "//mat-option[normalize-space(.) = \"" + scope + "\"]"));
            click(getDriver(), option);
        }
        escape(getDriver());
        waitFor(ofMillis(400L));
    }

    @Override
    public void addAndScheme(int cardIndex) {
        click(getDriver(), elementToBeClickable(getDriver(), By.xpath(
                card(cardIndex) + "//button[contains(@class, \"add-and-btn\")]")));
        waitFor(ofMillis(400L));
    }

    @Override
    public void addAlternative() {
        click(getDriver(), elementToBeClickable(getDriver(), ADD_ALTERNATIVE_BUTTON_LOCATOR));
        waitFor(ofMillis(400L));
    }

    @Override
    public void setAnonymous(int cardIndex, boolean checked) {
        WebElement checkbox = visibilityOfElementLocated(getDriver(), By.xpath(card(cardIndex) + "//mat-checkbox"));
        if (isChecked(checkbox) != checked) {
            click(getDriver(), checkbox.findElement(By.tagName("input")));
            waitFor(ofMillis(400L));
        }
    }

    @Override
    public boolean isDuplicateWarningDisplayed() {
        return isElementPresent(getDriver(), DUPLICATE_WARNING_LOCATOR);
    }

    @Override
    public String getDuplicateWarningText() {
        if (!isDuplicateWarningDisplayed()) {
            return "";
        }
        String text = getText(visibilityOfElementLocated(getDriver(), DUPLICATE_WARNING_LOCATOR));
        return text == null ? "" : text;
    }

    @Override
    public boolean isApplyEnabled() {
        WebElement button = visibilityOfElementLocated(getDriver(), APPLY_BUTTON_LOCATOR);
        String klass = button.getAttribute("class");
        return button.isEnabled() && (klass == null || !klass.contains("mat-mdc-button-disabled"));
    }

    @Override
    public void hitApply() {
        click(elementToBeClickable(getDriver(), APPLY_BUTTON_LOCATOR));
        invisibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
        waitFor(ofMillis(500L));
    }

    @Override
    public void cancel() {
        click(elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR));
        waitFor(ofMillis(500L));
    }
}
