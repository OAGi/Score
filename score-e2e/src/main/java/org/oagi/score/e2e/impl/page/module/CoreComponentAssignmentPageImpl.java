package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ModuleSetReleaseObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.module.CoreComponentAssignmentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class CoreComponentAssignmentPageImpl extends BasePageImpl implements CoreComponentAssignmentPage {
    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Den\")]//ancestor::mat-form-field//input");
    private static final By ASSIGN_BUTTON_LOCATOR =
            By.xpath("//button[@mattooltip=\"Assign\"]");

    private ModuleSetReleaseObject moduleSetRelease;

    public CoreComponentAssignmentPageImpl(BasePage parent, ModuleSetReleaseObject moduleSetRelease) {
        super(parent);
        this.moduleSetRelease = moduleSetRelease;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/module_management/module_set_release/" + this.moduleSetRelease.getModuleSetReleaseId() + "/assign").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Core Component Assignment".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }

    @Override
    public void setDen(String name) {
        sendKeys(getDenField(), name);
    }

    @Override
    public WebElement getDenField(){
        return visibilityOfElementLocated(getDriver(), DEN_FIELD_LOCATOR);
    }

    @Override
    public void selectCCByDEN(String name) {
        setDen(name);
        retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "den");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a Core Component using " + name, e);
            }
            String denColumn = getText(td.findElement(By.tagName("a")));
            if (!denColumn.contains(name)) {
                throw new NoSuchElementException("Cannot locate a Core Component using " + name);
            }
            WebElement select = getColumnByName(tr, "checkbox");
            click(select);
        });
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }
    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void hitAssignButton() {
        click(getAssignButton());
    }

    @Override
    public WebElement getAssignButton(){
        return elementToBeClickable(getDriver(), ASSIGN_BUTTON_LOCATOR);
    }
}
