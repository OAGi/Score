package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.bie.TransferBIEOwnershipDialogImpl;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.ASCCPObject;
import org.oagi.score.e2e.obj.BCCPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.TransferBIEOwnershipDialog;
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

    private static final By CC_TYPE_SELECT_FIELD_LOCATOR =
            By.xpath("//span[contains(text(),\"ACC, ASCCP, BCCP, CDT, BDT\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(),\"State\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

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
    public WebElement getTypeSelectField(){
        return visibilityOfElementLocated(getDriver(), CC_TYPE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getStateSelectField(){
        return visibilityOfElementLocated(getDriver(), STATE_SELECT_FIELD_LOCATOR);
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

    @Override
    public TransferCCOwnershipDialog openTransferCCOwnershipDialog(WebElement tr) {
        WebElement td = getColumnByName(tr, "transferOwnership");
        click(td.findElement(By.tagName("button")));

        TransferCCOwnershipDialog transferCCOwnershipDialog =
                new TransferCCOwnershipDialogImpl(this);
        assert transferCCOwnershipDialog.isOpened();
        return transferCCOwnershipDialog;
    }

    private WebElement getCreateComponentButton() {
        return elementToBeClickable(getDriver(), By.xpath("//button[contains(@mattooltip, \"Create Component\")]"));
    }

    @Override
    public WebElement getCreateACCButton() {
        click(getCreateComponentButton());
        return elementToBeClickable(getDriver(),
                By.xpath("//div[contains(@class, \"mat-menu-content\")]/button/span[text() = \"ACC\"]"));
    }

    @Override
    public ACCViewEditPage createACC(String branch) {
        setBranch(branch);
        click(getCreateACCButton());

        ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByDENAndReleaseNum("Object Class Term", branch);
        ACCViewEditPage accViewEditPage = new ACCViewEditPageImpl(this, acc);
        assert accViewEditPage.isOpened();
        return accViewEditPage;
    }

    @Override
    public WebElement getCreateASCCPButton() {
        click(getCreateComponentButton());
        return elementToBeClickable(getDriver(),
                By.xpath("//div[contains(@class, \"mat-menu-content\")]/button/span[text() = \"ASCCP\"]"));
    }

    @Override
    public ASCCPCreateDialog openASCCPCreateDialog(String branch) {
        setBranch(branch);
        click(getCreateASCCPButton());

        ASCCPCreateDialog asccpCreateDialog = new ASCCPCreateDialogImpl(this, branch);
        assert asccpCreateDialog.isOpened();
        return asccpCreateDialog;
    }

    @Override
    public WebElement getCreateBCCPButton() {
        click(getCreateComponentButton());
        return elementToBeClickable(getDriver(),
                By.xpath("//div[contains(@class, \"mat-menu-content\")]/button/span[text() = \"BCCP\"]"));
    }

    @Override
    public BCCPCreateDialog openBCCPCreateDialog(String branch) {
        setBranch(branch);
        click(getCreateBCCPButton());

        BCCPCreateDialog bccpCreateDialog = new BCCPCreateDialogImpl(this, branch);
        assert bccpCreateDialog.isOpened();
        return bccpCreateDialog;
    }

    @Override
    public WebElement getCreateDTButton() {
        click(getCreateComponentButton());
        return elementToBeClickable(getDriver(),
                By.xpath("//div[contains(@class, \"mat-menu-content\")]/button/span[text() = \"DT\"]"));
    }

    @Override
    public DTCreateDialog openDTCreateDialog(String branch) {
        setBranch(branch);
        click(getCreateDTButton());

        DTCreateDialog dtCreateDialog = new DTCreateDialogImpl(this, branch);
        assert dtCreateDialog.isOpened();
        return dtCreateDialog;
    }

    private void openCoreComponentByDen(String den) {
        sendKeys(getDENField(), den);
        hitSearchButton();

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
    public void hitSearchButton() {
        retry(() -> click(getSearchButton()));
        invisibilityOfLoadingContainerElement(getDriver());
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
        return getText(tableData.findElement(By.cssSelector("div.den")));
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
