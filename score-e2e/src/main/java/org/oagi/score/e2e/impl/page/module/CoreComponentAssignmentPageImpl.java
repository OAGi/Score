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
    private static final By DEN_UNASSIGNED_FIELD_LOCATOR =
            By.xpath("//h4[contains(text(),\"Unassigned\")]//ancestor::div[2]//mat-label[contains(text(), \"Den\")]//ancestor::mat-form-field//input");
    private static final By DEN_ASSIGNED_FIELD_LOCATOR =
            By.xpath("//h4[contains(text(),\"Assigned\")]//ancestor::div[2]//mat-label[contains(text(), \"Den\")]//ancestor::mat-form-field//input");
    private static final By ASSIGN_BUTTON_LOCATOR =
            By.xpath("//button[@mattooltip=\"Assign\"]");
    private static final By UNASSIGN_BUTTON_LOCATOR =
            By.xpath("//button[@mattooltip=\"Unassign\"]");

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
    public void setDenUnassigned(String name) {
        sendKeys(getDenUnassignedField(), name);
    }

    @Override
    public WebElement getDenAssignedField(){
        return visibilityOfElementLocated(getDriver(), DEN_ASSIGNED_FIELD_LOCATOR);
    }

    @Override
    public void setDenAssigned(String name) {
        sendKeys(getDenAssignedField(), name);
    }

    @Override
    public WebElement getDenUnassignedField(){
        return visibilityOfElementLocated(getDriver(), DEN_UNASSIGNED_FIELD_LOCATOR);
    }

    @Override
    public void selectUnassignedCCByDEN(String name) {
        setDenUnassigned(name);
        retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndexUnassignedCC(1);
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
    public void selectAssignedCCByDEN(String name) {
        setDenAssigned(name);
        retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndexAssignedCC(1);
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
    public WebElement getTableRecordByValueUnassignedCC(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//div[@class=\"assign-cc-table-wrapper\"][1]//td//a[text()=\"" + value + "\"]/ancestor::tr"));
    }

    @Override
    public WebElement getTableRecordByValueAssignedCC(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//div[@class=\"assign-cc-table-wrapper\"][2]//td//a[text()=\"" + value + "\"]/ancestor::tr"));
    }


    @Override
    public WebElement getTableRecordAtIndexAssignedCC(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//div[@class=\"assign-cc-table-wrapper\"][2]//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordAtIndexUnassignedCC(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//div[@class=\"assign-cc-table-wrapper\"][1]//tbody/tr[" + idx + "]"));
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

    @Override
    public void selectModule(String moduleName) {
        click(getModuleByName(moduleName));
    }

    @Override
    public WebElement getModuleByName(String moduleName) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//span[text() = \"" + moduleName + "\"]//ancestor::div[1]"));
    }

    @Override
    public void hitUnassignButton() {
        click(getUnassignButton());
    }

    @Override
    public WebElement getUnassignButton(){
        return elementToBeClickable(getDriver(), UNASSIGN_BUTTON_LOCATOR);
    }
}
