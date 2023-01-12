package org.oagi.score.e2e.impl.page.context;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.context.BusinessContextValueDialog;
import org.oagi.score.e2e.page.context.EditBusinessContextPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditBusinessContextPageImpl extends BasePageImpl implements EditBusinessContextPage {

    private static final By NAME_FIELD_LOCATOR
            = By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::div[1]/input");

    private static final By ADD_BUSINESS_CONTEXT_VALUE_BUTTON_LOCATOR
            = By.xpath("//mat-icon[contains(text(), \"add\")]//ancestor::button[1]");

    private static final By REMOVE_BUSINESS_CONTEXT_VALUE_BUTTON_LOCATOR
            = By.xpath("//span[contains(text(),\"Remove\")]//ancestor::button[1]");

    private static final By UPDATE_BUTTON_LOCATOR
            = By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");

    private final BusinessContextObject businessContext;

    public EditBusinessContextPageImpl(BasePage parent, BusinessContextObject businessContext) {
        super(parent);
        this.businessContext = businessContext;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/context_management/business_context/" + businessContext.getBusinessContextId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Edit Business Context".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }

    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//td[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    public WebElement getAddBusinessContextValueButton() {
        return elementToBeClickable(getDriver(), ADD_BUSINESS_CONTEXT_VALUE_BUTTON_LOCATOR);
    }

    public WebElement getRemoveBusinessContextValueButton() {
        return elementToBeClickable(getDriver(), REMOVE_BUSINESS_CONTEXT_VALUE_BUTTON_LOCATOR);
    }

    @Override
    public BusinessContextValueDialog openBusinessContextValueDialog() {
        click(getAddBusinessContextValueButton());
        BusinessContextValueDialog businessContextValueDialog = new BusinessContextValueDialogImpl(this);
        assert businessContextValueDialog.isOpened();
        return businessContextValueDialog;
    }

    @Override
    public BusinessContextValueDialog openBusinessContextValueDialog(BusinessContextValueObject businessContextValue) {
        ContextSchemeValueObject contextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().getContextSchemeValueById(businessContextValue.getContextSchemeValueId());
        return openBusinessContextValueDialogByContextSchemeValue(contextSchemeValue);
    }

    @Override
    public BusinessContextValueDialog openBusinessContextValueDialogByContextSchemeValue(ContextSchemeValueObject contextSchemeValue) {
        ContextSchemeObject contextScheme =
                getAPIFactory().getContextSchemeAPI().getContextSchemeById(contextSchemeValue.getOwnerContextSchemeId());
        ContextCategoryObject contextCategory =
                getAPIFactory().getContextCategoryAPI().getContextCategoryById(contextScheme.getContextCategoryId());

        WebElement td = visibilityOfElementLocated(getDriver(), By.xpath(
                "//tbody//td/span[contains(text(), \"" + contextSchemeValue.getValue() + "\")]" +
                        "/ancestor::tr/td[contains(text(), \"" + contextScheme.getSchemeName() + "\")]" +
                        "/ancestor::tr/td[contains(text(), \"" + contextCategory.getName() + "\")]"));
        click(td);
        BusinessContextValueDialog businessContextValueDialog = new BusinessContextValueDialogImpl(this);
        assert businessContextValueDialog.isOpened();
        retry(() -> {
            if (isEmpty(getText(businessContextValueDialog.getContextCategorySelectField()))) {
                throw new WebDriverException("Cannot locate a context category.");
            }
            if (isEmpty(getText(businessContextValueDialog.getContextSchemeSelectField()))) {
                throw new WebDriverException("Cannot locate a context scheme.");
            }
            if (isEmpty(getText(businessContextValueDialog.getContextSchemeValueSelectField()))) {
                throw new WebDriverException("Cannot locate a context scheme value.");
            }
        });
        return businessContextValueDialog;
    }

    @Override
    public void removeBusinessContextValue(BusinessContextValueObject businessContextValue) {
        ContextSchemeValueObject contextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().getContextSchemeValueById(businessContextValue.getContextSchemeValueId());
        removeBusinessContextValueByContextSchemeValue(contextSchemeValue);
    }

    @Override
    public void removeBusinessContextValueByContextSchemeValue(ContextSchemeValueObject contextSchemeValue) {
        ContextSchemeObject contextScheme =
                getAPIFactory().getContextSchemeAPI().getContextSchemeById(contextSchemeValue.getOwnerContextSchemeId());
        ContextCategoryObject contextCategory =
                getAPIFactory().getContextCategoryAPI().getContextCategoryById(contextScheme.getContextCategoryId());

        WebElement tr = getTableRecordByValue(contextCategory.getName());
        WebElement td = getColumnByName(tr, "select");
        click(td.findElement(By.xpath("mat-checkbox/label/span[1]")));
        click(getRemoveBusinessContextValueButton());

        assert "Remove Business Context?".equals(getDialogTitle(getDriver()));
        click(getDialogButtonByName(getDriver(), "Remove"));
    }

    @Override
    public WebElement getUpdateButton() {
        return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
    }

    @Override
    public void hitUpdateButton() {
        click(getUpdateButton());
        assert "Updated".equals(getSnackBarMessage(getDriver()));
    }
}
