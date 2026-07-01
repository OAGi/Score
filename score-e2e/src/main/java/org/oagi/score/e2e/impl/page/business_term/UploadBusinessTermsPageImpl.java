package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.business_term.UploadBusinessTermsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class UploadBusinessTermsPageImpl extends BasePageImpl implements UploadBusinessTermsPage {

    // Target the title span specifically: the mat-dialog-title lives on the header row, which also
    // contains the close (X) button, so reading the whole row would append the icon's "close" text.
    private static final By DIALOG_TITLE_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(@class, \"import-title\")]");

    private static final By DOWNLOAD_TEMPLATE_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Download template\")]//ancestor::button[1]");

    private static final By FILE_UPLOAD_INPUT_LOCATOR =
            By.xpath("//mat-dialog-container//input[contains(@class, \"file-input\")]");

    // A single-worksheet file auto-advances to the Map-columns step once the server-side parse
    // completes (#1754), so the upload step's row/column summary is no longer the signal we wait on;
    // the Map step's amber "Review the column mapping" notice is (its container keeps the .detected-format
    // class even though #1754 replaced the old "Detected source" text with a review-the-mapping prompt).
    private static final By DETECTED_FORMAT_LOCATOR =
            By.xpath("//mat-dialog-container//div[contains(@class, \"detected-format\")]");

    private static final By PREVIEW_SUMMARY_LOCATOR =
            By.xpath("//mat-dialog-container//div[contains(@class, \"preview-summary\")]");

    private static final By NEXT_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//button[normalize-space(.)=\"Next\"]");

    private static final By IMPORT_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//button[contains(normalize-space(.), \"Import\")]");

    private static final By READY_CHIP_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(@class, \"chip-valid\")]");

    private static final By REVIEW_CHIP_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(@class, \"chip-review\")]");

    private static final By RESULT_SUMMARY_LOCATOR =
            By.xpath("//mat-dialog-container//div[contains(@class, \"result-summary\")]");

    private static final By SHEET_SELECT_LOCATOR =
            By.xpath("//mat-dialog-container//mat-form-field[contains(@class, \"sheet-select\")]//mat-select");

    private static final By BACK_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//button[normalize-space(.)=\"Back\"]");

    private static final By DROP_ZONE_LOCATOR =
            By.xpath("//mat-dialog-container//div[contains(@class, \"drop-zone\")]");

    private static final By FILE_TILE_LOCATOR =
            By.xpath("//mat-dialog-container//div[contains(@class, \"file-tile\")]");

    private static final By FILE_NAME_LOCATOR =
            By.xpath("//mat-dialog-container//div[contains(@class, \"file-name\")]");

    private static final By FILE_REMOVE_LOCATOR =
            By.xpath("//mat-dialog-container//button[contains(@class, \"file-remove\")]");

    private static final By URI_BASE_INPUT_LOCATOR =
            By.xpath("//mat-dialog-container//mat-form-field[contains(@class, \"uri-base\")]//input");

    private static final By CLOSE_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//button[@aria-label=\"Cancel and close\"]");

    private static final By IMPORT_ERROR_LOCATOR =
            By.xpath("//mat-dialog-container//div[contains(@class, \"import-error\")]");

    private static final By DIALOG_CONTAINER_LOCATOR = By.cssSelector("mat-dialog-container");

    private final ViewEditBusinessTermPageImpl parent;

    public UploadBusinessTermsPageImpl(ViewEditBusinessTermPageImpl parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected String getPageUrl() {
        // The upload flow is a modal dialog rendered over the Business Term list; it has no own URL.
        return getConfig().getBaseUrl().resolve("/business_term_management/business_term").toString();
    }

    @Override
    public void openPage() {
        throw new UnsupportedOperationException(
                "The upload flow is a dialog; open it via ViewEditBusinessTermPage#hitUploadBusinessTermsButton().");
    }

    @Override
    public boolean isOpened() {
        WebElement title;
        try {
            title = getTitle();
        } catch (TimeoutException e) {
            return false;
        }
        return "Upload Business Terms".equals(getText(title));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), DIALOG_TITLE_LOCATOR);
    }

    @Override
    public WebElement getDownloadTemplateButton() {
        return elementToBeClickable(getDriver(), DOWNLOAD_TEMPLATE_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getFileUploadInput() {
        return findElement(getDriver(), FILE_UPLOAD_INPUT_LOCATOR);
    }

    @Override
    public void uploadFile(String absolutePath) {
        getFileUploadInput().sendKeys(absolutePath);
        // A single-worksheet file auto-advances from the upload step to the Map-columns step once the
        // server-side parse completes (#1754); wait for that step's amber verify-the-mapping notice.
        visibilityOfElementLocated(getDriver(), DETECTED_FORMAT_LOCATOR);
    }

    @Override
    public void hitNextButton() {
        click(elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR));
        waitFor(ofMillis(500L));
    }

    @Override
    public void proceedToPreview() {
        // uploadFile() already auto-advanced to the Map-columns step (#1754), so a single Next reaches
        // the Review & select step. Wait for that step's summary chips before returning.
        hitNextButton(); // map step -> review & select step
        visibilityOfElementLocated(getDriver(), PREVIEW_SUMMARY_LOCATOR);
    }

    @Override
    public WebElement getImportButton() {
        return elementToBeClickable(getDriver(), IMPORT_BUTTON_LOCATOR);
    }

    @Override
    public void hitImportButton() {
        click(getImportButton());
        // Wait for the result step.
        visibilityOfElementLocated(getDriver(), RESULT_SUMMARY_LOCATOR);
    }

    @Override
    public boolean isImportButtonEnabled() {
        return findElement(getDriver(), IMPORT_BUTTON_LOCATOR).isEnabled();
    }

    @Override
    public String getNeedReviewChipText() {
        return getText(visibilityOfElementLocated(getDriver(), REVIEW_CHIP_LOCATOR));
    }

    @Override
    public String getReadyChipText() {
        return getText(visibilityOfElementLocated(getDriver(), READY_CHIP_LOCATOR));
    }

    @Override
    public String getResultSummaryText() {
        return getText(visibilityOfElementLocated(getDriver(), RESULT_SUMMARY_LOCATOR));
    }

    @Override
    public void uploadMultiSheetFile(String absolutePath) {
        getFileUploadInput().sendKeys(absolutePath);
        // A multi-worksheet workbook does NOT auto-advance (#1754): the dialog stays on the upload step
        // so the user can pick the worksheet. Wait for that picker instead of the Map-columns notice.
        visibilityOfElementLocated(getDriver(), SHEET_SELECT_LOCATOR);
    }

    @Override
    public boolean isWorksheetSelectVisible() {
        try {
            return visibilityOfElementLocated(getDriver(), SHEET_SELECT_LOCATOR).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public void selectWorksheet(String sheetName) {
        click(elementToBeClickable(getDriver(), SHEET_SELECT_LOCATOR));
        // The mat-select options render in a CDK overlay outside the dialog container.
        By optionLocator = By.xpath("//mat-option[.//span[normalize-space(.)=\"" + sheetName + "\"]]");
        click(elementToBeClickable(getDriver(), optionLocator));
    }

    @Override
    public void waitForParsedSummary(int rowCount, int columnCount) {
        By locator = By.xpath("//mat-dialog-container//div[contains(@class, \"import-info\")]"
                + "[contains(normalize-space(.), \"" + rowCount + " row(s) and " + columnCount + " column(s)\")]");
        visibilityOfElementLocated(getDriver(), locator);
    }

    @Override
    public void hitBackButton() {
        click(elementToBeClickable(getDriver(), BACK_BUTTON_LOCATOR));
        waitFor(ofMillis(500L));
    }

    @Override
    public boolean isNextButtonEnabled() {
        return findElement(getDriver(), NEXT_BUTTON_LOCATOR).isEnabled();
    }

    @Override
    public void removeSelectedFile() {
        click(elementToBeClickable(getDriver(), FILE_REMOVE_LOCATOR));
        visibilityOfElementLocated(getDriver(), DROP_ZONE_LOCATOR);
    }

    @Override
    public boolean isDropZoneVisible() {
        try {
            return visibilityOfElementLocated(getDriver(), DROP_ZONE_LOCATOR).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public boolean isFileTileVisible() {
        try {
            return visibilityOfElementLocated(getDriver(), FILE_TILE_LOCATOR).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public String getSelectedFileName() {
        return getText(visibilityOfElementLocated(getDriver(), FILE_NAME_LOCATOR));
    }

    @Override
    public void sendFileToInput(String absolutePath) {
        getFileUploadInput().sendKeys(absolutePath);
    }

    @Override
    public String getSnackBarMessage() {
        // Fully qualified: a same-named no-arg member method would otherwise shadow the static import.
        return org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage(getDriver());
    }

    @Override
    public String getDropZoneErrorText() {
        try {
            return getText(visibilityOfElementLocated(getDriver(), IMPORT_ERROR_LOCATOR));
        } catch (TimeoutException e) {
            return "";
        }
    }

    @Override
    public void setSynthesizeBaseUrl(String baseUrl) {
        WebElement input = visibilityOfElementLocated(getDriver(), URI_BASE_INPUT_LOCATOR);
        input.clear();
        sendKeys(input, baseUrl);
    }

    @Override
    public boolean isSynthesizeModeActive() {
        try {
            return visibilityOfElementLocated(getDriver(), URI_BASE_INPUT_LOCATOR).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public String getMappingNoticeText() {
        return getText(visibilityOfElementLocated(getDriver(), DETECTED_FORMAT_LOCATOR));
    }

    @Override
    public void cancelViaCloseButton() {
        click(elementToBeClickable(getDriver(), CLOSE_BUTTON_LOCATOR));
        invisibilityOfElementLocated(getDriver(), DIALOG_CONTAINER_LOCATOR);
    }

    @Override
    public boolean isClosed() {
        return invisibilityOfElementLocated(getDriver(), DIALOG_CONTAINER_LOCATOR);
    }
}
