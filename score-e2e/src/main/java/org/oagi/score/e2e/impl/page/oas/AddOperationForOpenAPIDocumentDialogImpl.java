package org.oagi.score.e2e.impl.page.oas;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.oas.AddOperationForOpenAPIDocumentDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.escape;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfAllElementsLocatedBy;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

public class AddOperationForOpenAPIDocumentDialogImpl implements AddOperationForOpenAPIDocumentDialog {

    private static final String BASE_XPATH = "//mat-dialog-container";

    private static final By TITLE_LOCATOR =
            By.xpath(BASE_XPATH + "//*[contains(@class, \"mat-mdc-dialog-title\")]");
    private static final By SUBTITLE_LOCATOR =
            By.xpath(BASE_XPATH + "//p[contains(@class, \"score-text-muted\")]");
    private static final By VERB_SELECT_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[contains(text(), \"Verb\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By RESOURCE_NAME_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[contains(text(), \"Resource Name\")]//ancestor::mat-form-field[1]//input");
    private static final By OPERATION_ID_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[contains(text(), \"Operation ID\")]//ancestor::mat-form-field[1]//input");
    private static final By OPERATION_ID_HINT_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[contains(text(), \"Operation ID\")]//ancestor::mat-form-field[1]//mat-hint");
    private static final By TAG_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[contains(text(), \"Tag\")]//ancestor::mat-form-field[1]//input");
    private static final By SUMMARY_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[contains(text(), \"Summary\")]//ancestor::mat-form-field[1]//input");
    private static final By ADD_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[.//span[normalize-space(.) = \"Add\"]]");
    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[.//span[normalize-space(.) = \"Cancel\"]]");

    private final BasePageImpl parent;

    public AddOperationForOpenAPIDocumentDialogImpl(BasePageImpl parent) {
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
    public WebElement getSubtitle() {
        return visibilityOfElementLocated(getDriver(), SUBTITLE_LOCATOR);
    }

    @Override
    public WebElement getVerbSelectField() {
        return visibilityOfElementLocated(getDriver(), VERB_SELECT_LOCATOR);
    }

    @Override
    public void setVerb(String verb) {
        click(getDriver(), getVerbSelectField());
        WebElement option = elementToBeClickable(getDriver(),
                By.xpath("//mat-option[normalize-space(.) = \"" + verb + "\"]"));
        click(getDriver(), option);
        waitFor(ofMillis(300L));
    }

    @Override
    public List<String> getVerbOptions() {
        click(getDriver(), getVerbSelectField());
        List<String> options = visibilityOfAllElementsLocatedBy(getDriver(), By.xpath("//mat-option"))
                .stream()
                .map(option -> getText(option).trim())
                .collect(Collectors.toList());
        escape(getDriver());
        waitFor(ofMillis(300L));
        return options;
    }

    @Override
    public WebElement getResourceNameField() {
        return visibilityOfElementLocated(getDriver(), RESOURCE_NAME_FIELD_LOCATOR);
    }

    @Override
    public void setResourceName(String resourceName) {
        sendKeys(getResourceNameField(), resourceName);
    }

    @Override
    public WebElement getOperationIdField() {
        return visibilityOfElementLocated(getDriver(), OPERATION_ID_FIELD_LOCATOR);
    }

    @Override
    public String getOperationId() {
        return getOperationIdField().getAttribute("value");
    }

    @Override
    public void setOperationId(String operationId) {
        sendKeys(getOperationIdField(), operationId);
    }

    @Override
    public String getOperationIdHint() {
        return getText(visibilityOfElementLocated(getDriver(), OPERATION_ID_HINT_LOCATOR));
    }

    @Override
    public void setTag(String tag) {
        sendKeys(visibilityOfElementLocated(getDriver(), TAG_FIELD_LOCATOR), tag);
    }

    @Override
    public void setSummary(String summary) {
        sendKeys(visibilityOfElementLocated(getDriver(), SUMMARY_FIELD_LOCATOR), summary);
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
        click(getAddButton(true));
        // On success the dialog closes (the operation is added); wait for it to disappear.
        invisibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
        waitFor(ofMillis(500L));
    }

    @Override
    public void cancel() {
        click(elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR));
        waitFor(ofMillis(500L));
    }
}
