package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.bie.TransferBIEOwnershipDialogImpl;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.TransferBIEOwnershipDialog;
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditCoreComponentPageImpl extends BasePageImpl implements ViewEditCoreComponentPage {

    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Branch\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By CC_TYPE_SELECT_FIELD_LOCATOR =
            By.xpath("//span[contains(text(),\"ACC, ASCCP, BCCP, CDT, BDT\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(),\"State\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By COMPONENT_TYPE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(),\"Component Type\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated end date\")]");

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
            waitFor(ofSeconds(2L));
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
    public void setTypeSelect(String type) {

        click(getTypeSelectField());
        waitFor(ofMillis(2000L));
        WebElement optionField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[text() = \"" + type + "\"]"));
        click(optionField);
    }

    @Override
    public WebElement getStateSelectField(){
        return visibilityOfElementLocated(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getUpdatedStartDateField() {
        return visibilityOfElementLocated(getDriver(), UPDATED_START_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setUpdatedStartDate(LocalDateTime updatedStartDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sendKeys(getUpdatedStartDateField(), formatter.format(updatedStartDate));
    }

    @Override
    public WebElement getUpdatedEndDateField() {
        return visibilityOfElementLocated(getDriver(), UPDATED_END_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setUpdatedEndDate(LocalDateTime updatedEndDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sendKeys(getUpdatedEndDateField(), formatter.format(updatedEndDate));
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
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@data-placeholder, \"Definition\")]"));
    }

    @Override
    public void setDefinition(String definition) {
        sendKeys(getDefinitionField(), definition);
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
    public WebElement getComponentTypeSelectField() {
        return elementToBeClickable(getDriver(), COMPONENT_TYPE_SELECT_FIELD_LOCATOR);
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
    public ACCViewEditPage openACCViewEditPageByManifestID(BigInteger accManifestID) {
        ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestID);
        ACCViewEditPage accViewEditPage = new ACCViewEditPageImpl(this, acc);
        accViewEditPage.openPage();
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
    public ASCCPViewEditPage openASCCPViewEditPageByManifestID(BigInteger asccpManifestID) {
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestID);
        ASCCPViewEditPage asccpViewEditPage = new ASCCPViewEditPageImpl(this, asccp);
        asccpViewEditPage.openPage();
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
    public BCCPViewEditPage openBCCPViewEditPageByManifestID(BigInteger bccpManifestID) {
        BCCPObject bccp = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestID);
        BCCPViewEditPage bccpViewEditPage = new BCCPViewEditPageImpl(this, bccp);
        bccpViewEditPage.openPage();
        assert bccpViewEditPage.isOpened();
        return bccpViewEditPage;
    }

    @Override
    public DTViewEditPage openDTViewEditPageByDenAndBranch(String den, String branch) {
        setBranch(branch);
        openCoreComponentByDen(den);

        List<DTObject> dtList = getAPIFactory().getCoreComponentAPI().getBDTByDENAndReleaseNum(den, branch);
        if (dtList.size() > 1) {
            throw new IllegalArgumentException("Found out more than one DT record by given arguments [DEN: " + den + ", Branch: " + branch + "]");
        }
        DTViewEditPage dtViewEditPage = new DTViewEditPageImpl(this, dtList.get(0));
        assert dtViewEditPage.isOpened();
        return dtViewEditPage;
    }

    @Override
    public DTViewEditPage openDTViewEditPageByManifestID(BigInteger dtManifestID) {
        DTObject dt = getAPIFactory().getCoreComponentAPI().getBDTByManifestId(dtManifestID);
        DTViewEditPage dtViewEditPage = new DTViewEditPageImpl(this, dt);
        dtViewEditPage.openPage();
        assert dtViewEditPage.isOpened();
        return dtViewEditPage;
    }

    @Override
    public TransferCCOwnershipDialog openTransferCCOwnershipDialog(WebElement tr) {
        WebElement td = getColumnByName(tr, "transferOwnership");
        click(td.findElement(By.className("mat-icon")));

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
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger accManifestId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
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

        retry(() -> {
            hitSearchButton();

            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordByValue(den);
                td = getColumnByName(tr, "den");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a core component using " + den, e);
            }
            String denField = getDENFieldFromTheTable(td);
            if (!den.equals(denField)) {
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
        waitFor(ofMillis(3000L));
        retry(() -> {
            click(getSearchButton());
            waitFor(ofMillis(1000L));
        });
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
        return getText(tableData.findElement(By.cssSelector("div.den > a > span")));
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

    @Override
    public void selectAllComponentTypes() {
        click(getTypeSelectField());
        ArrayList<String> componentTypes = new ArrayList<>(List.of("ACC", "ASCCP", "BCCP", "CDT", "BDT", "ASCC", "BCC"));
        boolean selected;
        for (String componentType : componentTypes) {
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[text()=\"" + componentType + "\"]//ancestor::mat-option"));
            if (optionField.getAttribute("aria-selected").equals("true")) {
                selected = true;
            } else {
                selected = false;
            }
            if (!selected) {
                click(optionField);
            }
        }
        escape(getDriver());
    }

    public DTViewEditPage createDT(String den, String branch) {
        setBranch(branch);
        click(getCreateDTButton());
        waitFor(ofMillis(2000L));

        DTCreateDialog dtCreateDialog = new DTCreateDialogImpl(this, branch);
        assert dtCreateDialog.isOpened();
        dtCreateDialog.selectBasedDTByDEN(den);
        dtCreateDialog.hitCreateButton();
        waitFor(ofMillis(2000L));
        invisibilityOfLoadingContainerElement(getDriver());

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger dtManifestId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));

        DTObject dt = getAPIFactory().getCoreComponentAPI().getBDTByManifestId(dtManifestId);
        DTViewEditPage dtViewEditPage = new DTViewEditPageImpl(this, dt);
        assert dtViewEditPage.isOpened();
        return dtViewEditPage;
    }

    @Override
    public BCCPViewEditPage createBCCP(String dataType, String branch, AppUserObject user) {
        setBranch(branch);
        click(getCreateBCCPButton());
        BCCPCreateDialog bccpCreateDialog = new BCCPCreateDialogImpl(this, branch);
        bccpCreateDialog.selectDataTypeByDEN(dataType);
        bccpCreateDialog.hitCreateButton();
        BCCPObject bccp = getAPIFactory().getCoreComponentAPI().getLatestBCCPCreatedByUser(user, branch);
        BCCPViewEditPage bccpViewEditPage = new BCCPViewEditPageImpl(this, bccp);
        assert bccpViewEditPage.isOpened();
        return bccpViewEditPage;
    }
}
