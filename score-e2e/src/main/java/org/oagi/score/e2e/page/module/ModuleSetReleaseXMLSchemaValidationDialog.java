package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface ModuleSetReleaseXMLSchemaValidationDialog extends Dialog {

    /**
     * Return the UI element of the progress bar.
     *
     * @return the UI element of the progress bar
     */
    WebElement getProgressBar();

    void hitCopyToClipboardButton();

    WebElement getCopyToClipboardButton();
}
