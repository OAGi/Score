package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface EditModuleDirectoryDialog extends Dialog {
    void setModuleDirectoryName(String directoryName);

    WebElement getModuleDirectoryNameField();

    void updateModuleDirectory();

    WebElement getUpdateModuleDirectoryButton();

    void discardDirectory();

    WebElement getDiscardModuleDirectoryButton();
}
