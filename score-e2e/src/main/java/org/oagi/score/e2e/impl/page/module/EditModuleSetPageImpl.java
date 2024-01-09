package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ModuleSetObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.module.*;
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
        return visibilityOfElementLocated(getDriver(), By.className("mat-mdc-card-title"));
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
    public CreateModuleFileDialog addNewModuleFile() {
        click(getAddNewModuleFileButton());
        CreateModuleFileDialog createModuleFileDialog = new CreateModuleFileDialogImpl(this);
        assert createModuleFileDialog.isOpened();
        return createModuleFileDialog;
    }
    private WebElement getAddNewModuleButton() {
        return elementToBeClickable(getDriver(), By.xpath("//mat-mdc-card-content//span[contains(text(), \"Add\")]"));
    }

    private WebElement getAddNewModuleFileButton() {
        return elementToBeClickable(getDriver(), By.xpath("//mat-expansion-panel//mat-panel-title[contains(text(), \"Create new module file\")]"));
    }


    @Override
    public EditModuleFileDialog editModuleFile(String moduleFileName) {
        retry(() -> {
            click(getModuleByName(moduleFileName));
            waitFor(ofMillis(1000L));

            click(getModuleEditLink(moduleFileName));
            waitFor(ofMillis(1000L));
        });

        EditModuleFileDialog editModuleFileDialog = new EditModuleFileDialogImpl(this);
        assert editModuleFileDialog.isOpened();
        return  editModuleFileDialog;
    }

    @Override
    public WebElement getModuleEditLink(String moduleName) {
        return elementToBeClickable(getDriver(), By.xpath("//*[text() = \"" + moduleName + "\"]//ancestor::div[2]//mat-icon[contains(text(), \"edit\")]"));
    }

    @Override
    public WebElement getModuleByName(String moduleName) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//*[text() = \"" + moduleName + "\"]//ancestor::div[1]"));
    }

    @Override
    public CreateModuleDirectoryDialog addNewModuleDirectory() {
        click(getAddNewModuleDirectoryButton());
        CreateModuleDirectoryDialog createModuleDirectoryDialog = new CreateModuleDirectoryDialogImpl(this);
        assert createModuleDirectoryDialog.isOpened();
        return createModuleDirectoryDialog;
    }

    private WebElement getAddNewModuleDirectoryButton() {
        return elementToBeClickable(getDriver(), By.xpath("//mat-expansion-panel//mat-panel-title[contains(text(), \"Create new module directory\")]"));
    }

    @Override
    public EditModuleDirectoryDialog editModuleDirectory(String moduleDirectoryName) {
        retry(() -> {
            click(getModuleByName(moduleDirectoryName));
            waitFor(ofMillis(1000L));

            click(getModuleEditLink(moduleDirectoryName));
            waitFor(ofMillis(1000L));
        });

        EditModuleDirectoryDialog editModuleDirectoryDialog = new EditModuleDirectoryDialogImpl(this);
        assert editModuleDirectoryDialog.isOpened();
        return editModuleDirectoryDialog;
    }

    @Override
    public CopyModuleFromExistingModuleSetDialog copyFromExistingModuleSet() {
        click(getCopyFromExistingModuleSetButton());
        CopyModuleFromExistingModuleSetDialog copyModuleFromExistingModuleSetDialog =
                new CopyModuleFromExistingModuleSetDialogImpl(this);
        assert copyModuleFromExistingModuleSetDialog.isOpened();
        return copyModuleFromExistingModuleSetDialog;
    }
    private WebElement getCopyFromExistingModuleSetButton() {
        return elementToBeClickable(getDriver(), By.xpath("//mat-expansion-panel//mat-panel-title[contains(text(), \"Copy from existing module set\")]"));
    }

    @Override
    public WebElement continueToDiscard() {
        return null;
    }
}
