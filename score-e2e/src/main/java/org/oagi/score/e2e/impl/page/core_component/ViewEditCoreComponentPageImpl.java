package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.ASCCPObject;
import org.oagi.score.e2e.obj.BCCPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditCoreComponentPageImpl extends BasePageImpl implements ViewEditCoreComponentPage {

    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Branch\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

    public ViewEditCoreComponentPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/core_component").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Core Component".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setBranch(String branch) {
        retry(() -> {
            click(getBranchSelectField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[text() = \"" + branch + "\"]"));
            click(optionField);
        });
    }

    @Override
    public WebElement getDENField() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@data-placeholder, \"DEN\")]"));
    }

    @Override
    public String getDENFieldLabel() {
        return getDENField().getAttribute("data-placeholder");
    }

    @Override
    public void setDEN(String den) {
        sendKeys(getDENField(), den);
    }

    @Override
    public WebElement getModuleField() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@data-placeholder, \"Module\")]"));
    }

    @Override
    public String getModuleFieldLabel() {
        return getModuleField().getAttribute("data-placeholder");
    }

    @Override
    public void setModule(String module) {
        sendKeys(getModuleField(), module);
    }

    @Override
    public ACCViewEditPage openACCViewEditPageByDenAndBranch(String den, String branch) {
        setBranch(branch);
        openCoreComponentByDen(den);

        ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByDENAndReleaseNum(den, branch);
        ACCViewEditPage accViewEditPage = new ACCViewEditPageImpl(this, acc);
        assert accViewEditPage.isOpened();
        return accViewEditPage;
    }

    @Override
    public ASCCPViewEditPage openASCCPViewEditPageByDenAndBranch(String den, String branch) {
        setBranch(branch);
        openCoreComponentByDen(den);

        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(den, branch);
        ASCCPViewEditPage asccpViewEditPage = new ASCCPViewEditPageImpl(this, asccp);
        assert asccpViewEditPage.isOpened();
        return asccpViewEditPage;
    }

    @Override
    public BCCPViewEditPage openBCCPViewEditPageByDenAndBranch(String den, String branch) {
        setBranch(branch);
        openCoreComponentByDen(den);

        BCCPObject bccp = getAPIFactory().getCoreComponentAPI().getBCCPByDENAndReleaseNum(den, branch);
        BCCPViewEditPage bccpViewEditPage = new BCCPViewEditPageImpl(this, bccp);
        assert bccpViewEditPage.isOpened();
        return bccpViewEditPage;
    }

    @Override
    public DTViewEditPage openDTViewEditPageByDenAndBranch(String den, String branch) {
        setBranch(branch);
        openCoreComponentByDen(den);

        throw new UnsupportedOperationException();
    }

    private void openCoreComponentByDen(String den) {
        sendKeys(getDENField(), den);
        click(getSearchButton());
        invisibilityOfLoadingContainerElement(getDriver());
        retry(() -> {
            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "den");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a core component using " + den, e);
            }
            if (!den.equals(getDENFieldFromTheTable(td))) {
                throw new NoSuchElementException("Cannot locate a core component using " + den);
            }
            WebElement tdLoginID = td.findElement(By.cssSelector("a"));
            // TODO:
            // 'click' does not work when the browser hides the link.
            getDriver().get(tdLoginID.getAttribute("href"));
        });
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        defaultWait(getDriver());
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value){
        defaultWait(getDriver());
        return visibilityOfElementLocated(getDriver(), By.xpath("//*[contains(text(),\""+value+"\")]//ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    private String getDENFieldFromTheTable(WebElement tableData) {
        WebElement span = tableData.findElement(By.cssSelector("span.den"));
        return span.getAttribute("innerHTML");
    }

    @Override
    public int getNumberOfOnlyCCsPerStateAreListed(String state) {
        return getDriver().findElements(By.xpath("//table//*[contains(text(), \"" + state + "\")][contains(@class, '"+state+"')]")).size();
    }

    @Override
    public WebElement getTableRecordByCCNameAndOwner(String name, String owner){
        defaultWait(getDriver());
        return visibilityOfElementLocated(getDriver(), By.xpath("//*[contains(text(),\""+name+"\")]//ancestor::tr//td[8]//*[contains(text(),\""+owner+"\")]"));
    }

    @Override
    public void setItemsPerPage(int items) {
        WebElement itemsPerPageField = elementToBeClickable(getDriver(),
                By.xpath("//div[.=\" Items per page: \"]/following::div[5]"));
        click(itemsPerPageField);
        waitFor(Duration.ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(itemField);
        waitFor(Duration.ofMillis(500L));
    }
}
