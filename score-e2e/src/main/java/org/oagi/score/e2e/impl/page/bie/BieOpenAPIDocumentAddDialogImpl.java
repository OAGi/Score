package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.bie.BieOpenAPIDocumentAddDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.escape;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.isChecked;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/**
 * Implementation of the BIE-root 'Add to OpenAPI Document' dialog (Issue #1519).
 */
public class BieOpenAPIDocumentAddDialogImpl implements BieOpenAPIDocumentAddDialog {

    private static final String BASE_XPATH = "//mat-dialog-container";

    private static final By TITLE_LOCATOR =
            By.xpath(BASE_XPATH + "//*[contains(@class, \"mat-mdc-dialog-title\")]");
    private static final By DOCUMENT_SELECT_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"OpenAPI Document\"]//ancestor::mat-form-field[1]//mat-select");
    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label = \"dropdown search\"]");
    private static final By VERB_SELECT_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"Verb\"]//ancestor::mat-form-field[1]//mat-select");
    private static final By MESSAGE_BODY_SELECT_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"Message Body\"]//ancestor::mat-form-field[1]//mat-select");
    private static final By RESOURCE_NAME_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"Resource Name\"]//ancestor::mat-form-field[1]//input");
    private static final By OPERATION_ID_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[normalize-space(.) = \"Operation ID\"]//ancestor::mat-form-field[1]//input");
    private static final By ARRAY_CHECKBOX_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-checkbox[contains(normalize-space(.), \"Make as an array\")]");
    private static final By SUPPRESS_ROOT_CHECKBOX_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-checkbox[contains(normalize-space(.), \"Suppress a root property\")]");
    private static final By ADD_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[.//span[normalize-space(.) = \"Add\"]]");
    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[.//span[normalize-space(.) = \"Cancel\"]]");

    private final BasePageImpl parent;

    public BieOpenAPIDocumentAddDialogImpl(BasePageImpl parent) {
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
    public void selectOpenAPIDocument(String title) {
        click(getDriver(), visibilityOfElementLocated(getDriver(), DOCUMENT_SELECT_LOCATOR));
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), title);
        waitFor(ofMillis(500L));
        WebElement option = elementToBeClickable(getDriver(), By.xpath(
                "//mat-option[.//span[contains(normalize-space(.), " +
                        org.oagi.score.e2e.impl.PageHelper.xpathLiteral(title) + ")]]"));
        click(getDriver(), option);
        waitFor(ofMillis(300L));
    }

    @Override
    public void setVerb(String verb) {
        click(getDriver(), visibilityOfElementLocated(getDriver(), VERB_SELECT_LOCATOR));
        WebElement option = elementToBeClickable(getDriver(),
                By.xpath("//mat-option[.//span[normalize-space(.) = \"" + verb + "\"]]"));
        click(getDriver(), option);
        waitFor(ofMillis(300L));
    }

    @Override
    public void setMessageBody(String messageBody) {
        click(getDriver(), visibilityOfElementLocated(getDriver(), MESSAGE_BODY_SELECT_LOCATOR));
        WebElement option = elementToBeClickable(getDriver(),
                By.xpath("//mat-option[.//span[normalize-space(.) = \"" + messageBody + "\"]]"));
        click(getDriver(), option);
        waitFor(ofMillis(300L));
    }

    @Override
    public boolean isMessageBodyOptionDisabled(String messageBody) {
        click(getDriver(), visibilityOfElementLocated(getDriver(), MESSAGE_BODY_SELECT_LOCATOR));
        WebElement option = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option[.//span[normalize-space(.) = \"" + messageBody + "\"]]"));
        boolean disabled = "true".equals(option.getAttribute("aria-disabled"))
                || option.getAttribute("class").contains("mdc-list-item--disabled")
                || option.getAttribute("class").contains("mat-mdc-option-disabled");
        escape(getDriver());
        waitFor(ofMillis(300L));
        return disabled;
    }

    @Override
    public void setArrayIndicator(boolean checked) {
        WebElement checkbox = visibilityOfElementLocated(getDriver(), ARRAY_CHECKBOX_LOCATOR);
        if (isChecked(checkbox) != checked) {
            click(getDriver(), checkbox.findElement(By.tagName("input")));
        }
    }

    @Override
    public void setSuppressRoot(boolean checked) {
        WebElement checkbox = visibilityOfElementLocated(getDriver(), SUPPRESS_ROOT_CHECKBOX_LOCATOR);
        if (isChecked(checkbox) != checked) {
            click(getDriver(), checkbox.findElement(By.tagName("input")));
        }
    }

    @Override
    public String getResourceNamePreview() {
        return visibilityOfElementLocated(getDriver(), RESOURCE_NAME_FIELD_LOCATOR).getAttribute("value");
    }

    @Override
    public String getOperationIdPreview() {
        return visibilityOfElementLocated(getDriver(), OPERATION_ID_FIELD_LOCATOR).getAttribute("value");
    }

    @Override
    public String getDuplicateBodyError() {
        java.util.List<WebElement> errors = getDriver().findElements(
                By.xpath(BASE_XPATH + "//div[contains(@class, \"oas-add-duplicate-error\")]"));
        return (errors.isEmpty() || !errors.get(0).isDisplayed()) ? "" : errors.get(0).getText();
    }

    @Override
    public WebElement getAddButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), ADD_BUTTON_LOCATOR);
        }
        return visibilityOfElementLocated(getDriver(), ADD_BUTTON_LOCATOR);
    }

    @Override
    public void hitAddButton() {
        click(getDriver(), getAddButton(true));
        invisibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
        waitFor(ofMillis(500L));
    }

    @Override
    public void cancel() {
        try {
            click(getDriver(), elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR));
        } catch (NoSuchElementException | TimeoutException e) {
            escape(getDriver());
        }
        invisibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
    }
}
