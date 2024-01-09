package org.oagi.score.e2e.impl.page.context;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessContextValueObject;
import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.obj.ContextSchemeObject;
import org.oagi.score.e2e.obj.ContextSchemeValueObject;
import org.oagi.score.e2e.page.context.BusinessContextValueDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class BusinessContextValueDialogImpl implements BusinessContextValueDialog {

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By CONTEXT_CATEGORY_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Context Category\")]//ancestor::mat-form-field//mat-select");

    private static final By CONTEXT_CATEGORY_DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Context Category\")]//ancestor::mat-form-field/ancestor::mat-card-content/mat-form-field[2]//textarea");

    private static final By CONTEXT_SCHEME_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Context Scheme\")]//ancestor::mat-form-field//mat-select");

    private static final By CONTEXT_SCHEME_ID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Scheme ID\")]//ancestor::div[1]/input");

    private static final By CONTEXT_SCHEME_AGENCY_ID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Agency ID\")]//ancestor::div[1]/input");

    private static final By CONTEXT_SCHEME_VERSION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Version\")]//ancestor::div[1]/input");

    private static final By CONTEXT_SCHEME_DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Context Scheme\")]//ancestor::mat-form-field/ancestor::mat-card-content/mat-form-field[5]//textarea");
    private static final By CONTEXT_SCHEME_VALUE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Context Scheme Value\")]//ancestor::mat-form-field//mat-select");

    private static final By CONTEXT_SCHEME_VALUE_MEANING_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Context Scheme Value\")]//ancestor::mat-form-field/ancestor::mat-card-content/mat-form-field[2]//textarea");

    private static final By ADD_BUTTON_LOCATOR
            = By.xpath("//mat-dialog-container//mat-icon[contains(text(), \"add\")]//ancestor::button[1]");

    private static final By SAVE_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Save\")]//ancestor::button[1]");

    private final BasePageImpl parent;

    public BusinessContextValueDialogImpl(BasePageImpl parent) {
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
    public WebElement getContextCategorySelectField() {
        return visibilityOfElementLocated(getDriver(), CONTEXT_CATEGORY_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setContextCategory(ContextCategoryObject contextCategory) {
        click(getContextCategorySelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), contextCategory.getName());
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + contextCategory.getName() + "\")]"));
        click(searchedSelectField);
    }

    @Override
    public WebElement getContextCategoryDescriptionField() {
        return visibilityOfElementLocated(getDriver(), CONTEXT_CATEGORY_DESCRIPTION_FIELD_LOCATOR);
    }

    @Override
    public WebElement getContextSchemeSelectField() {
        return visibilityOfElementLocated(getDriver(), CONTEXT_SCHEME_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setContextScheme(ContextSchemeObject contextScheme) {
        click(getContextSchemeSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), contextScheme.getSchemeName());
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + contextScheme.getSchemeName() + "\")]"));
        click(searchedSelectField);
    }

    @Override
    public WebElement getContextSchemeIDField() {
        return visibilityOfElementLocated(getDriver(), CONTEXT_SCHEME_ID_FIELD_LOCATOR);
    }

    @Override
    public WebElement getContextSchemeAgencyIDField() {
        return visibilityOfElementLocated(getDriver(), CONTEXT_SCHEME_AGENCY_ID_FIELD_LOCATOR);
    }

    @Override
    public WebElement getContextSchemeVersionField() {
        return visibilityOfElementLocated(getDriver(), CONTEXT_SCHEME_VERSION_FIELD_LOCATOR);
    }

    @Override
    public WebElement getContextSchemeDescriptionField() {
        return visibilityOfElementLocated(getDriver(), CONTEXT_SCHEME_DESCRIPTION_FIELD_LOCATOR);
    }

    @Override
    public WebElement getContextSchemeValueSelectField() {
        return visibilityOfElementLocated(getDriver(), CONTEXT_SCHEME_VALUE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setContextSchemeValue(ContextSchemeValueObject contextSchemeValue) {
        click(getContextSchemeValueSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), contextSchemeValue.getValue());
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + contextSchemeValue.getValue() + "\")]"));
        click(searchedSelectField);
    }

    @Override
    public WebElement getContextSchemeValueMeaningField() {
        return visibilityOfElementLocated(getDriver(), CONTEXT_SCHEME_VALUE_MEANING_FIELD_LOCATOR);
    }

    @Override
    public WebElement getAddButton() {
        return elementToBeClickable(getDriver(), ADD_BUTTON_LOCATOR);
    }

    @Override
    public void addBusinessContextValue(BusinessContextValueObject businessContextValue) {

    }

    @Override
    public WebElement getSaveButton() {
        return elementToBeClickable(getDriver(), SAVE_BUTTON_LOCATOR);
    }

    @Override
    public void updateBusinessContextValue(BusinessContextValueObject businessContextValue) {

    }

    @Override
    public void close() {
        escape(getDriver());
    }
}
