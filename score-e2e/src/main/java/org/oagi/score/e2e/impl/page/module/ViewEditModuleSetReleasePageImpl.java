package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ModuleSetReleaseObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.module.CreateModuleSetReleasePage;
import org.oagi.score.e2e.page.module.EditModuleSetReleasePage;
import org.oagi.score.e2e.page.module.ViewEditModuleSetReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.click;

public class ViewEditModuleSetReleasePageImpl extends BasePageImpl implements ViewEditModuleSetReleasePage {
    private static final By NEW_MODULE_SET_RELEASE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Module Set Release\")]//ancestor::button[1]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Name\")]//ancestor::mat-form-field//input");

    public ViewEditModuleSetReleasePageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/module_management/module_set_release").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Module Set Release".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public CreateModuleSetReleasePage hitNewModuleSetReleaseButton() {
        retry(() -> click(getNewModuleSetReleaseButton()));
        CreateModuleSetReleasePage createModuleSetReleasePage = new CreateModuleSetReleasePageImpl(this);
        assert createModuleSetReleasePage.isOpened();
        return createModuleSetReleasePage;
    }

    @Override
    public WebElement getNewModuleSetReleaseButton() {
        return elementToBeClickable(getDriver(), NEW_MODULE_SET_RELEASE_BUTTON_LOCATOR);
    }

    @Override
    public EditModuleSetReleasePage openModuleSetReleaseByName(ModuleSetReleaseObject moduleSetRelease) {
        return openModuleSetReleaseByName(moduleSetRelease.getName());
    }

    @Override
    public EditModuleSetReleasePage openModuleSetReleaseByName(String moduleSetReleaseName) {
        setName(moduleSetReleaseName);
        hitSearchButton();

        retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "name");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a Module Set Release using " + moduleSetReleaseName, e);
            }
            String nameColumn = getText(td.findElement(By.tagName("a")));
            if (!nameColumn.contains(moduleSetReleaseName)) {
                throw new NoSuchElementException("Cannot locate a Module Set Release using " + moduleSetReleaseName);
            }
            WebElement tdReleaseName = td.findElement(By.cssSelector("a"));
            // TODO:
            // 'click' does not work when the browser hides the link.
            getDriver().get(tdReleaseName.getAttribute("href"));
        });
        waitFor(ofMillis(1000L));

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger moduleSetReleaseId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        ModuleSetReleaseObject moduleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getModuleSetReleaseById(moduleSetReleaseId);
        EditModuleSetReleasePage editModuleSetReleasePage = new EditModuleSetReleasePageImpl(this, moduleSetRelease);
        assert editModuleSetReleasePage.isOpened();
        return editModuleSetReleasePage;
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
        return visibilityOfElementLocated(getDriver(), By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//tbody/tr[" + idx + "]"));
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
}
