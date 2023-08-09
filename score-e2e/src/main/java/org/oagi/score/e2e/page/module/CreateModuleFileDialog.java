package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface CreateModuleFileDialog extends Dialog {
    WebElement getModuleFileNameField();

    void setModuleFileName(String moduleFileName);

    void setNamespace(String namespaceURI);

    void setModuleFileVersionNumber(String moduleFileVersion);

    WebElement getNamespaceSelectField();

    WebElement getNamespaceField();

    WebElement getModuleFileVersionNumberField();

    void createModuleFile();

    WebElement getCreateModuleFileButton();

}
