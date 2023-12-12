package org.oagi.score.e2e.impl.page.context;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.obj.ContextSchemeObject;
import org.oagi.score.e2e.obj.ContextSchemeValueObject;
import org.oagi.score.e2e.page.context.ContextSchemeValueDialog;
import org.oagi.score.e2e.page.context.CreateContextSchemePage;
import org.oagi.score.e2e.page.context.LoadFromCodeListDialog;
import org.oagi.score.e2e.page.context.ViewEditContextSchemePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateContextSchemePageImpl extends BasePageImpl implements CreateContextSchemePage {

    private static final By CONTEXT_CATEGORY_SELECT_FIELD_LOCATOR
            = By.xpath("//mat-select[@placeholder=\"Context Category\"]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By NAME_FIELD_LOCATOR
            = By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::div[1]/input");

    private static final By LOAD_FROM_CODE_LIST_BUTTON_LOCATOR
            = By.xpath("//span[contains(text(), \"Load from Code List\")]//ancestor::button[1]");

    private static final By SCHEME_ID_FIELD_LOCATOR
            = By.xpath("//mat-label[contains(text(), \"Scheme ID\")]//ancestor::div[1]/input");

    private static final By AGENCY_ID_FIELD_LOCATOR
            = By.xpath("//mat-label[contains(text(), \"Agency ID\")]//ancestor::div[1]/input");

    private static final By VERSION_FIELD_LOCATOR
            = By.xpath("//mat-label[contains(text(), \"Version\")]//ancestor::div[1]/input");

    private static final By DESCRIPTION_FIELD_LOCATOR
            = By.xpath("//mat-label[.=\"Description\"]/ancestor::div[1]/textarea[1]");

    private static final By ADD_CONTEXT_SCHEME_VALUE_BUTTON_LOCATOR
            = By.xpath("//mat-icon[contains(text(), \"add\")]//ancestor::button[1]");

    private static final By REMOVE_CONTEXT_SCHEME_VALUE_BUTTON_LOCATOR
            = By.xpath("//span[contains(text(), \"Remove\")]//ancestor::button[1]");

    private static final By CREATE_BUTTON_LOCATOR
            = By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");

    private final ViewEditContextSchemePageImpl parent;

    public CreateContextSchemePageImpl(ViewEditContextSchemePageImpl parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/context_management/context_scheme/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Create Context Scheme".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-mdc-card-title"));
    }

    @Override
    public WebElement getContextCategorySelectField() {
        return visibilityOfElementLocated(getDriver(), CONTEXT_CATEGORY_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setContextCategory(ContextCategoryObject contextCategory) {
        retry(() -> {
            click(getContextCategorySelectField());
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), contextCategory.getName());
            WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[contains(text(), \"" + contextCategory.getName() + "\")]"));
            click(searchedSelectField);
        });
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
    public WebElement getLoadFromCodeListButton() {
        return elementToBeClickable(getDriver(), LOAD_FROM_CODE_LIST_BUTTON_LOCATOR);
    }

    @Override
    public LoadFromCodeListDialog openLoadFromCodeListDialog() {
        return retry(() -> {
            click(getLoadFromCodeListButton());
            LoadFromCodeListDialog loadFromCodeListDialog = new LoadFromCodeListDialogImpl(this);
            assert loadFromCodeListDialog.isOpened();
            return loadFromCodeListDialog;
        });
    }

    @Override
    public WebElement getSchemeIDField() {
        return visibilityOfElementLocated(getDriver(), SCHEME_ID_FIELD_LOCATOR);
    }

    @Override
    public void setSchemeID(String schemeID) {
        sendKeys(getSchemeIDField(), schemeID);
    }

    @Override
    public WebElement getAgencyIDField() {
        return visibilityOfElementLocated(getDriver(), AGENCY_ID_FIELD_LOCATOR);
    }

    @Override
    public void setAgencyID(String agencyID) {
        sendKeys(getAgencyIDField(), agencyID);
    }

    @Override
    public WebElement getVersionField() {
        return visibilityOfElementLocated(getDriver(), VERSION_FIELD_LOCATOR);
    }

    @Override
    public void setVersion(String version) {
        sendKeys(getVersionField(), version);
    }

    @Override
    public WebElement getDescriptionField() {
        return visibilityOfElementLocated(getDriver(), DESCRIPTION_FIELD_LOCATOR);
    }

    @Override
    public void setDescription(String description) {
        sendKeys(getDescriptionField(), description);
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

    public WebElement getAddContextSchemeValueButton() {
        return elementToBeClickable(getDriver(), ADD_CONTEXT_SCHEME_VALUE_BUTTON_LOCATOR);
    }

    public WebElement getRemoveContextSchemeValueButton() {
        return elementToBeClickable(getDriver(), REMOVE_CONTEXT_SCHEME_VALUE_BUTTON_LOCATOR);
    }

    @Override
    public ContextSchemeValueDialog openContextSchemeValueDialog() {
        return retry(() -> {
            click(getAddContextSchemeValueButton());
            ContextSchemeValueDialog contextSchemeValueDialog = new ContextSchemeValueDialogImpl(this);
            assert contextSchemeValueDialog.isOpened();
            return contextSchemeValueDialog;
        });
    }

    @Override
    public ContextSchemeValueDialog openContextSchemeValueDialog(ContextSchemeValueObject contextSchemeValue) {
        return openContextSchemeValueDialogByValue(contextSchemeValue.getValue());
    }

    @Override
    public ContextSchemeValueDialog openContextSchemeValueDialogByValue(String value) {
        return retry(() -> {
            WebElement td = elementToBeClickable(getDriver(), By.xpath("//tbody//td[contains(text(), \"" + value + "\")]"));
            click(td);
            ContextSchemeValueDialog contextSchemeValueDialog = new ContextSchemeValueDialogImpl(this);
            assert contextSchemeValueDialog.isOpened();
            return contextSchemeValueDialog;
        });
    }

    @Override
    public boolean isContextSchemeValueChecked(String value) {
        WebElement tr = getTableRecordByValue(value);
        WebElement td = getColumnByName(tr, "select");
        WebElement checkbox = td.findElement(By.xpath("mat-checkbox"));
        return checkbox.getAttribute("class").contains("mat-checkbox-checked");
    }

    @Override
    public void toggleContextSchemeValue(String value) {
        WebElement tr = getTableRecordByValue(value);
        WebElement td = getColumnByName(tr, "select");
        click(td.findElement(By.xpath("mat-checkbox/label/span[1]")));
    }

    @Override
    public void removeContextSchemeValue(ContextSchemeValueObject contextSchemeValue) {
        removeContextSchemeValue(contextSchemeValue.getValue());
    }

    @Override
    public void removeContextSchemeValue(String value) {
        if (!isContextSchemeValueChecked(value)) {
            toggleContextSchemeValue(value);
        }
        click(getRemoveContextSchemeValueButton());

        assert "Remove Context Scheme Value?".equals(getDialogTitle(getDriver()));
        click(getDialogButtonByName(getDriver(), "Remove"));
    }

    @Override
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public void hitCreateButton() {
        retry(() -> {
            click(getCreateButton());
        });
        assert getSnackBar(getDriver(), "Created").isDisplayed();
    }

    @Override
    public ViewEditContextSchemePage createContextScheme(ContextCategoryObject contextCategory,
                                                         ContextSchemeObject contextScheme) {
        return createContextScheme(contextCategory, contextScheme, Collections.emptyList());
    }

    @Override
    public ViewEditContextSchemePage createContextScheme(ContextCategoryObject contextCategory,
                                                         ContextSchemeObject contextScheme,
                                                         List<ContextSchemeValueObject> contextSchemeValueList) {
        setContextCategory(contextCategory);
        setName(contextScheme.getSchemeName());
        setSchemeID(contextScheme.getSchemeId());
        setAgencyID(contextScheme.getSchemeAgencyId());
        setVersion(contextScheme.getSchemeVersionId());
        setDescription(contextScheme.getDescription());
        for (ContextSchemeValueObject contextSchemeValue : contextSchemeValueList) {
            ContextSchemeValueDialog contextSchemeValueDialog = openContextSchemeValueDialog();
            contextSchemeValueDialog.addContextSchemeValue(contextSchemeValue);
        }
        hitCreateButton();
        return this.parent;
    }
}
