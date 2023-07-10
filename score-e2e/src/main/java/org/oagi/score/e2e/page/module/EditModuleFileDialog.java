package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface EditModuleFileDialog extends Dialog {
    void updateModuleFile();
    WebElement getUpdateModuleFileButton();
    WebElement getModuleFileNameField();
    void setModuleFileName(String moduleFileName);

    void setNamespace(String namespaceURI);
    void setModuleFileVersionNumber(String moduleFileVersion);

    WebElement getNamespaceSelectField();

    WebElement getModuleFileVersionNumberField();

    void discardFile();

    WebElement getDiscardModuleFileButton();

    WebElement getContinueToDiscardFileButton();

    String getDiscardFileMessage();
}
