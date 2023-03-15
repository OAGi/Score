package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditCodeListValueDialogImpl implements EditCodeListValueDialog {
    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");
    private static final By CODE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Code\")]//ancestor::mat-form-field//input");
    private static final By MEANING_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Meaning\")]//ancestor::mat-form-field//input");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By ADD_CODE_LIST_VALUE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Add\")]//ancestor::mat-dialog-actions/button[1]");

    private final BasePageImpl parent;

    public EditCodeListValueDialogImpl(BasePageImpl parent) {
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
    public void setCode(String code) {
        sendKeys(getCodeField(), code);
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
    public WebElement getCodeField() {
        return visibilityOfElementLocated(getDriver(), CODE_FIELD_LOCATOR);
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
}
