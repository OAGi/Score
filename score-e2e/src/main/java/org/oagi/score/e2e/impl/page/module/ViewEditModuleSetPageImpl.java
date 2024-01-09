package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ModuleSetObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.module.CreateModuleSetPage;
import org.oagi.score.e2e.page.module.EditModuleSetPage;
import org.oagi.score.e2e.page.module.ViewEditModuleSetPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditModuleSetPageImpl extends BasePageImpl implements ViewEditModuleSetPage {
    private static final By NEW_MODULE_SET_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Module Set\")]//ancestor::button[1]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By DISCARD_MODULE_SET_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Discard\")]");
    private static final By CONTINUE_TO_DISCARD_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button/span");

    public ViewEditModuleSetPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/module_management/module_set").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Module Set".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getNewModuleSetButton() {
        return elementToBeClickable(getDriver(), NEW_MODULE_SET_BUTTON_LOCATOR);
    }

    @Override
    public CreateModuleSetPage hitNewModuleSetButton() {
        retry(() -> click(getNewModuleSetButton()));
        CreateModuleSetPage editModuleSetPage = new CreateModuleSetPageImpl(this);
        assert editModuleSetPage.isOpened();
        return editModuleSetPage;
    }

    @Override
    public EditModuleSetPage openModuleSetByName(ModuleSetObject moduleSet) {
        return openModuleSetByName(moduleSet.getName());
    }

    @Override
    public EditModuleSetPage openModuleSetByName(String moduleSetName) {
        setName(moduleSetName);
        hitSearchButton();

        retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "name");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a Module Set using " + moduleSetName, e);
            }
            String nameColumn = getText(td.findElement(By.tagName("a")));
            if (!nameColumn.contains(moduleSetName)) {
                throw new NoSuchElementException("Cannot locate a Module Set using " + moduleSetName);
            }
            WebElement tdModuleName = td.findElement(By.cssSelector("a"));
            // TODO:
            // 'click' does not work when the browser hides the link.
            getDriver().get(tdModuleName.getAttribute("href"));
        });

        ModuleSetObject moduleSet = getAPIFactory().getModuleSetAPI().getModuleSetByName(moduleSetName);
        EditModuleSetPage editModuleSetPage = new EditModuleSetPageImpl(this, moduleSet);
        assert editModuleSetPage.isOpened();
        return editModuleSetPage;
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        click(getSearchButton());
        waitFor(ofMillis(500L));
    }
    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-mdc-card-content//tbody/tr[" + idx + "]"));
    }
    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }
    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }
    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public void discardModuleSet(String moduleSetName) {
        setName(moduleSetName);
        hitSearchButton();
        retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "name");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a Module Set using " + moduleSetName, e);
            }
            String nameColumn = getText(td.findElement(By.tagName("a")));
            if (!nameColumn.contains(moduleSetName)) {
                throw new NoSuchElementException("Cannot locate a Module Set using " + moduleSetName);
            }
            WebElement node = clickOnDropDownMenu(tr);
            try {
                click(elementToBeClickable(getDriver(), DISCARD_MODULE_SET_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(elementToBeClickable(getDriver(), DISCARD_MODULE_SET_OPTION_LOCATOR));
            }
        });

        click(elementToBeClickable(getDriver(), CONTINUE_TO_DISCARD_BUTTON_IN_DIALOG_LOCATOR));
        waitFor(ofMillis(500L));

        assert "Discarded".equals(getSnackBarMessage(getDriver()));

    }
    @Override
    public WebElement clickOnDropDownMenu(WebElement element) {
        return element.findElement(By.xpath("//mat-icon[contains(text(), \"more_vert\")]"));
    }
}
