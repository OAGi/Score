package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ModuleSetObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.module.EditModuleSetPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;

public class EditModuleSetPageImpl extends BasePageImpl implements EditModuleSetPage {
    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Description\")]//ancestor::mat-form-field//textarea");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By MODULE_FILE_NAME_FIELD_LOCATOR =
            By.xpath("//mat-expansion-panel//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By MODULE_FILE_VERSION_FIELD_LOCATOR =
            By.xpath("//mat-expansion-panel//mat-label[contains(text(), \"Version\")]//ancestor::mat-form-field//input");
    private static final By NAMESPACE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-expansion-panel//*[text()= \"Namespace\"]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By NAMESPACE_FIELD_LOCATOR =
            By.xpath("//mat-expansion-panel//*[text()= \"Namespace\"]//ancestor::mat-form-field[1]//mat-select");
    private static final By CREATE_MODULE_FILE_BUTTON_LOCATOR =
            By.xpath("//mat-expansion-panel//span[contains(text(), \"Create\")]//ancestor::button[1][@ng-reflect-disabled=\"false\"]");
    private ModuleSetObject moduleSet;
    public EditModuleSetPageImpl(BasePage parent, ModuleSetObject moduleSet) {
        super(parent);
        this.moduleSet = moduleSet;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/module_management/module_set/" + this.moduleSet.getModuleSetId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Edit Module Set".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }

    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }

    @Override
    public void setDescription(String description) {
        sendKeys(getDescriptionField(), description);
    }

    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDescriptionField() {
        return visibilityOfElementLocated(getDriver(), DESCRIPTION_FIELD_LOCATOR);
    }

    @Override
    public void hitUpdateButton() {
        retry(() -> {
            click(getUpdateButton(true));
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
        assert "Updated".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getUpdateButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), UPDATE_BUTTON_LOCATOR);
        }
    }

    @Override
    public void addModule() {
        click(getAddNewModuleButton());
    }

    @Override
    public void addNewModuleFile() {
        click(getAddNewModuleFileButton());
    }
    private WebElement getAddNewModuleButton() {
        return elementToBeClickable(getDriver(), By.xpath("//mat-card-content//span[contains(text(), \"Add\")]"));
    }

    private WebElement getAddNewModuleFileButton() {
        return elementToBeClickable(getDriver(), By.xpath("//mat-expansion-panel//mat-panel-title[contains(text(), \"Create new module file\")]"));
    }

    @Override
    public WebElement getModuleFileNameField() {
        return visibilityOfElementLocated(getDriver(), MODULE_FILE_NAME_FIELD_LOCATOR);
    }

    @Override
    public void setModuleFileName(String moduleFileName) {
        sendKeys(getModuleFileNameField(), moduleFileName);
    }

    @Override
    public void setNamespace(String namespaceURI) {
        retry(() -> {
            click(getNamespaceSelectField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + namespaceURI + "\")]//ancestor::mat-option[1]/span"));
            click(optionField);
            waitFor(ofMillis(500L));
        });
    }

    @Override
    public void setModuleFileVersionNumber(String moduleFileVersion) {
        sendKeys(getModuleFileVersionNumberField(), moduleFileVersion);
    }

    @Override
    public WebElement getNamespaceSelectField() {
        return visibilityOfElementLocated(getDriver(), NAMESPACE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getNamespaceField() {
        return visibilityOfElementLocated(getDriver(), NAMESPACE_FIELD_LOCATOR);
    }

    @Override
    public WebElement getModuleFileVersionNumberField() {
        return visibilityOfElementLocated(getDriver(), MODULE_FILE_VERSION_FIELD_LOCATOR);
    }

    @Override
    public void createModuleFile() {
        click(getCreateModuleFileButton());
    }

    @Override
    public WebElement getCreateModuleFileButton() {
        return elementToBeClickable(getDriver(), CREATE_MODULE_FILE_BUTTON_LOCATOR);
    }
}
