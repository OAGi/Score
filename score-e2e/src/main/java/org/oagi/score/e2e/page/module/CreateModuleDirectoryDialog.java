package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface CreateModuleDirectoryDialog extends Dialog {
    void setModuleDirectoryName(String moduleDirectoryName);

    WebElement getModuleDirectoryNameField();

    void createModuleDirectory();

    WebElement getCreateModuleDirectoryButton();
}
