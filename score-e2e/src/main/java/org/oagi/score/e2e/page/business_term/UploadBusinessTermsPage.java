package org.oagi.score.e2e.page.business_term;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Upload Business Terms' page.
 */
public interface UploadBusinessTermsPage extends Page {

    /**
     * Return the UI element of the 'Download template' button.
     *
     * @return the UI element of the 'Download template' button
     */
    WebElement getDownloadTemplateButton();

    /**
     * Return the input element for a file upload.
     *
     * @return the input element for a file upload
     */
    WebElement getFileUploadInput();

    /**
     * Return the UI element of the 'Attach' button with Paper Clip icon.
     *
     * @return the UI element of the 'Attach' button
     */
    WebElement getAttachButton();

}
