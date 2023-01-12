package org.oagi.score.e2e.impl.page.context;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ContextSchemeValueObject;
import org.oagi.score.e2e.page.context.ContextSchemeValueDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ContextSchemeValueDialogImpl implements ContextSchemeValueDialog {

    private static final By VALUE_FIELD_LOCATOR =
            By.xpath("//mat-label[.=\"Value\"]//ancestor::div[1]/input[1]");

    private static final By MEANING_FIELD_LOCATOR =
            By.xpath("//mat-label[.=\"Meaning\"]//ancestor::div[1]/textarea[1]");

    private static final By ADD_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Add\")]//ancestor::mat-dialog-actions[1]/button[1]");

    private static final By SAVE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Save\")]//ancestor::button[1]");

    private final BasePageImpl parent;

    public ContextSchemeValueDialogImpl(BasePageImpl parent) {
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
    public WebElement getValueField() {
        return visibilityOfElementLocated(getDriver(), VALUE_FIELD_LOCATOR);
    }

    @Override
    public void setValue(String value) {
        sendKeys(getValueField(), value);
    }

    @Override
    public WebElement getMeaningField() {
        return visibilityOfElementLocated(getDriver(), MEANING_FIELD_LOCATOR);
    }

    @Override
    public void setMeaning(String meaning) {
        sendKeys(getMeaningField(), meaning);
    }

    @Override
    public WebElement getAddButton() {
        return elementToBeClickable(getDriver(), ADD_BUTTON_LOCATOR);
    }

    @Override
    public void addContextSchemeValue(ContextSchemeValueObject contextSchemeValue) {
        setValue(contextSchemeValue.getValue());
        setMeaning(contextSchemeValue.getMeaning());
        click(getAddButton());
    }

    @Override
    public WebElement getSaveButton() {
        return elementToBeClickable(getDriver(), SAVE_BUTTON_LOCATOR);
    }

    @Override
    public void updateContextSchemeValue(ContextSchemeValueObject contextSchemeValue) {
        setValue(contextSchemeValue.getValue());
        setMeaning(contextSchemeValue.getMeaning());
        click(getSaveButton());
    }

}
