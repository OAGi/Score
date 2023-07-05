package org.oagi.score.e2e.impl.page.agency_id_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListValueDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditAgencyIDListValueDialogImpl implements EditAgencyIDListValueDialog {
    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//mat-dialog-content//mat-label[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");
    private static final By VALUE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Value\")]//ancestor::mat-form-field//input");
    private static final By MEANING_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Meaning\")]//ancestor::mat-form-field//input");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//mat-dialog-content//mat-label[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By ADD_CODE_LIST_VALUE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Add\")]//ancestor::mat-dialog-actions/button[1]");
    private static final By SAVE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Save\")]//ancestor::mat-dialog-actions/button[1]");
    private static final By DEPRECATED_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-dialog-content//span[contains(text(),\"Deprecated\")]//ancestor::mat-checkbox");
    private final BasePageImpl parent;

    public EditAgencyIDListValueDialogImpl(BasePageImpl parent) {
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
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//mat-card-title"));
    }

    @Override
    public void setValue(String value) {
        sendKeys(getValueField(), value);
    }

    @Override
    public void setMeaning(String meaning) {
        sendKeys(getMeaningField(), meaning);
    }

    @Override
    public void hitAddButton() {
        retry(() -> {
            click(getAddCodeListValueButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getValueField() {
        return visibilityOfElementLocated(getDriver(), VALUE_FIELD_LOCATOR);
    }

    @Override
    public WebElement getMeaningField() {
        return visibilityOfElementLocated(getDriver(), MEANING_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDefinitionSourceField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_SOURCE_FIELD_LOCATOR);
    }

    @Override
    public WebElement getAddCodeListValueButton() {
        return elementToBeClickable(getDriver(), ADD_CODE_LIST_VALUE_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getDeprecatedSelectField() {
        return visibilityOfElementLocated(getDriver(), DEPRECATED_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setDefinition(String definition) {
        sendKeys(getDefinitionField(), definition);
    }

    @Override
    public void setDefinitionSource(String definitionSource) {
        sendKeys(getDefinitionSourceField(), definitionSource);
    }

    @Override
    public void hitSaveButton() {
        retry(() -> {
            click(getSaveButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getSaveButton() {
        return elementToBeClickable(getDriver(), SAVE_BUTTON_LOCATOR);
    }
}
