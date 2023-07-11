package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface ModuleSetReleaseXMLSchemaValidationDialog extends Dialog {
    void hitCopyToClipboardButton();

    WebElement getCopyToClipboardButton();
}
