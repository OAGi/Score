package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.page.BaseSearchBarPageImpl;
import org.oagi.score.e2e.obj.DTObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.core_component.DTCreateDialog;
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.TransferCCOwnershipDialog;
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditDataTypePageImpl extends BaseSearchBarPageImpl implements ViewEditDataTypePage {

    public static final By CONTINUE_UPDATE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button");
    public static final By CONTINUE_TO_DELETE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Delete\")]//ancestor::button");
    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"branch-selector\")]//mat-select[1]");
    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[text() = \"Owner\"]//ancestor::mat-form-field[1]//mat-select");
    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated start date\")]");
    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated end date\")]");

    public ViewEditDataTypePageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/data_type").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        waitFor(ofSeconds(2L));
        assert "Data Type".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getBranchSelectField() {
        return elementToBeClickable(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setBranch(String branch) {
        retry(() -> {
            click(getDriver(), getBranchSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[@class = \"cdk-overlay-container\"]//mat-option//span[text() = \"" + branch + "\"]"));
            click(getDriver(), optionField);
            escape(getDriver());
        });

        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getStateSelectField() {
        return visibilityOfElementLocated(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setState(String state) {
        click(getDriver(), getStateSelectField());
        waitFor(ofMillis(2000L));
        WebElement optionField = elementToBeClickable(getDriver(),
                By.xpath("//span[.=\"" + state + "\"]//ancestor::mat-option[1]"));
        click(getDriver(), optionField);
        escape(getDriver());
    }

    @Override
    public WebElement getOwnerSelectField() {
        return visibilityOfElementLocated(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        retry(() -> {
            click(getDriver(), getOwnerSelectField());
            waitFor(ofMillis(2000L));
            WebElement optionField = elementToBeClickable(getDriver(),
                    By.xpath("//span[.=\"" + owner + "\"]//ancestor::mat-option[1]"));
            click(getDriver(), optionField);
            escape(getDriver());
        });
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
        return getInputFieldInSearchBar();
    }

    @Override
    public String getDENFieldLabel() {
        return getDENField().getAttribute("placeholder");
    }

    @Override
    public void setDEN(String den) {
        sendKeys(getDENField(), den);
    }

    @Override
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@placeholder, \"Definition\")]"));
    }

    @Override
    public void setDefinition(String definition) {
        sendKeys(getDefinitionField(), definition);
    }

    @Override
    public WebElement getModuleField() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@placeholder, \"Module\")]"));
    }

    @Override
    public String getModuleFieldLabel() {
        return getModuleField().getAttribute("placeholder");
    }

    @Override
    public void setModule(String module) {
        sendKeys(getModuleField(), module);
    }

    @Override
    public DTViewEditPage openDTViewEditPageByDenAndBranch(String den, String branch) {
        setBranch(branch);
        openCoreComponentByDen(den);
        // @TODO: Retrieve the name of the library from the UI.
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        List<DTObject> dtList = getAPIFactory().getCoreComponentAPI().getBDTByDENAndReleaseNum(library, den, branch);
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
        WebElement td = getColumnByName(tr, "owner");
        click(td.findElement(By.className("mat-icon")));

        TransferCCOwnershipDialog transferCCOwnershipDialog =
                new TransferCCOwnershipDialogImpl(this);
        assert transferCCOwnershipDialog.isOpened();
        return transferCCOwnershipDialog;
    }

    @Override
    public WebElement getNewDataTypeButton() {
        return elementToBeClickable(getDriver(), By.xpath("//button[contains(@mattooltip, \"New Data Type\")]"));
    }

    @Override
    public DTCreateDialog openDTCreateDialog(String branch) {
        setBranch(branch);
        click(getNewDataTypeButton());

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
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//*[contains(text(), \"" + value + "\")]//ancestor::tr"));
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
        return getDriver().findElements(By.xpath("//table//*[contains(text(), \"" + state + "\")][contains(@class, '" + state + "')]")).size();
    }

    @Override
    public WebElement getTableRecordByCCNameAndOwner(String name, String owner) {
        waitFor(ofMillis(1000L));
        return visibilityOfElementLocated(getDriver(), By.xpath("//*[contains(text(), \"" + name + "\")]//ancestor::tr//td[8]//*[contains(text(), \"" + owner + "\")]"));
    }

    @Override
    public void setItemsPerPage(int items) {
        WebElement itemsPerPageField = elementToBeClickable(getDriver(),
                By.xpath("//div[.=\" Items per page: \"]/following::mat-form-field//mat-select"));
        click(getDriver(), itemsPerPageField);
        waitFor(Duration.ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(getDriver(), itemField);
        waitFor(Duration.ofMillis(500L));
    }

    public DTViewEditPage createDT(String den, String branch) {
        setBranch(branch);
        click(getDriver(), getNewDataTypeButton());
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
    public WebElement getMoveToQAButton() {
        return elementToBeClickable(getDriver(), By.xpath("//button[contains(@mattooltip, \"Move to QA\")]"));
    }

    @Override
    public void hitMoveToQAButton() {
        click(getMoveToQAButton());
        click(elementToBeClickable(getDriver(), CONTINUE_UPDATE_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Updated".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getMoveToProductionButton() {
        return elementToBeClickable(getDriver(), By.xpath("//button[contains(@mattooltip, \"Move to Production\")]"));
    }

    @Override
    public void hitMoveToProductionButton() {
        click(getMoveToProductionButton());
        click(elementToBeClickable(getDriver(), CONTINUE_UPDATE_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Updated".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getBackToWIPButton() {
        return elementToBeClickable(getDriver(), By.xpath("//button[contains(@mattooltip, \"Back to WIP\")]"));
    }

    @Override
    public void hitBackToWIPButton() {
        click(getBackToWIPButton());
        click(elementToBeClickable(getDriver(), CONTINUE_UPDATE_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Updated".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public void hitMoveToDraftButton() {
        click(getMoveToDraftButton());
        click(elementToBeClickable(getDriver(), CONTINUE_UPDATE_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Updated".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getMoveToDraftButton() {
        return elementToBeClickable(getDriver(), By.xpath("//button[contains(@mattooltip, \"Move to Draft\")]"));
    }

    @Override
    public void hitMoveToCandidateButton() {
        click(getMoveToCandidateButton());
        click(elementToBeClickable(getDriver(), CONTINUE_UPDATE_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Updated".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getMoveToCandidateButton() {
        return elementToBeClickable(getDriver(), By.xpath("//button[contains(@mattooltip, \"Move to Candidate\")]"));
    }

    @Override
    public TransferCCOwnershipDialog hitTransferOwnershipButton() {
        click(getTransferOwnershipButton());
        TransferCCOwnershipDialog transferCCOwnershipDialog =
                new TransferCCOwnershipDialogImpl(this);
        assert transferCCOwnershipDialog.isOpened();
        return transferCCOwnershipDialog;
    }

    @Override
    public WebElement getTransferOwnershipButton() {
        return elementToBeClickable(getDriver(), By.xpath("//button[contains(@mattooltip, \"Transfer Ownership\")]"));
    }

    @Override
    public void hitDeleteButton() {
        retry(() -> {
            click(getDeleteButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_DELETE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getDeleteButton() {
        return elementToBeClickable(getDriver(), By.xpath("//button[contains(@mattooltip, \"Delete\")]"));
    }
}
