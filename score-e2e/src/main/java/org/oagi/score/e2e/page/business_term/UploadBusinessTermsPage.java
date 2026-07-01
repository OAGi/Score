package org.oagi.score.e2e.page.business_term;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of the 'Upload Business Terms' dialog, opened from the Business Term list. The dialog
 * walks through four steps: upload a file, map columns, review &amp; select rows, and a result
 * summary.
 */
public interface UploadBusinessTermsPage extends Page {

    /**
     * Return the UI element of the 'Download template' button (step 1).
     *
     * @return the UI element of the 'Download template' button
     */
    WebElement getDownloadTemplateButton();

    /**
     * Return the hidden file {@code <input>} element used to upload the source file (step 1).
     *
     * @return the file input element
     */
    WebElement getFileUploadInput();

    /**
     * Upload the file at the given absolute path and wait for the server-side parse to complete. A
     * single-worksheet file auto-advances to the 'Map columns' step once parsed, so on return the
     * dialog is on the mapping step rather than the upload step.
     *
     * @param absolutePath absolute path of the CSV/TSV/XLSX file to upload
     */
    void uploadFile(String absolutePath);

    /**
     * Click the 'Next' button to advance to the next step of the dialog.
     */
    void hitNextButton();

    /**
     * Advance from the 'Map columns' step (reached automatically after {@link #uploadFile(String)})
     * to the review &amp; select step, assuming the columns auto-mapped (e.g. the native template).
     */
    void proceedToPreview();

    /**
     * Return the 'Import N selected' button (review step).
     *
     * @return the import button
     */
    WebElement getImportButton();

    /**
     * Click the 'Import N selected' button and wait for the result step.
     */
    void hitImportButton();

    /**
     * @return whether the 'Import N selected' button is currently enabled
     */
    boolean isImportButtonEnabled();

    /**
     * @return the text of the 'need review' chip on the review step (e.g. "2 need review")
     */
    String getNeedReviewChipText();

    /**
     * @return the text of the 'ready' chip on the review step (e.g. "3 ready")
     */
    String getReadyChipText();

    /**
     * @return the text of the result-summary chips on the result step (created / updated / failed)
     */
    String getResultSummaryText();

    /**
     * Upload a multi-worksheet {@code .xlsx} and wait on the upload step's worksheet picker. A workbook
     * with more than one sheet does NOT auto-advance, so on return the dialog is still on the upload
     * step (unlike {@link #uploadFile(String)}).
     *
     * @param absolutePath absolute path of the multi-worksheet .xlsx file
     */
    void uploadMultiSheetFile(String absolutePath);

    /**
     * @return whether the worksheet picker (shown only for a multi-worksheet workbook) is visible on
     * the upload step
     */
    boolean isWorksheetSelectVisible();

    /**
     * Pick the given worksheet from the upload step's worksheet picker; this triggers a server-side
     * re-parse of that sheet.
     *
     * @param sheetName the worksheet to select
     */
    void selectWorksheet(String sheetName);

    /**
     * Wait until the upload step's "Found N row(s) and M column(s)" summary reports the given counts,
     * confirming a (re-)parse has completed before the caller advances.
     *
     * @param rowCount    expected data-row count
     * @param columnCount expected column count
     */
    void waitForParsedSummary(int rowCount, int columnCount);

    /**
     * Click the 'Back' button to return to the previous step of the dialog.
     */
    void hitBackButton();

    /**
     * @return whether the 'Next' button is currently enabled
     */
    boolean isNextButtonEnabled();

    /**
     * Remove the selected file via the file tile's remove (X) control and wait for the drag-and-drop
     * zone to return.
     */
    void removeSelectedFile();

    /**
     * @return whether the drag-and-drop zone (shown when no file is selected) is visible
     */
    boolean isDropZoneVisible();

    /**
     * @return whether the selected-file tile is visible on the upload step
     */
    boolean isFileTileVisible();

    /**
     * @return the file name shown on the selected-file tile
     */
    String getSelectedFileName();

    /**
     * Send a file to the upload input WITHOUT expecting the dialog to advance — used to exercise a
     * rejected replacement pick (an unsupported or oversized file). The caller then inspects the
     * snackbar via {@link #getSnackBarMessage()}.
     *
     * @param absolutePath absolute path of the file to send
     */
    void sendFileToInput(String absolutePath);

    /**
     * @return the current snackbar message text (e.g. a rejected-file message)
     */
    String getSnackBarMessage();

    /**
     * Enter the base URL used to synthesize the External Reference URI on the map step (visible only
     * in the 'Build from base URL + ID' strategy).
     *
     * @param baseUrl the base URL to type
     */
    void setSynthesizeBaseUrl(String baseUrl);

    /**
     * @return whether the map step is in the 'Build from base URL + ID' (synthesize) URI strategy
     */
    boolean isSynthesizeModeActive();

    /**
     * @return the text of the map step's amber "Review the column mapping" notice
     */
    String getMappingNoticeText();

    /**
     * Cancel the import via the top-right close (X) button and wait for the dialog to close.
     */
    void cancelViaCloseButton();

    /**
     * @return whether the import dialog has closed
     */
    boolean isClosed();
}
