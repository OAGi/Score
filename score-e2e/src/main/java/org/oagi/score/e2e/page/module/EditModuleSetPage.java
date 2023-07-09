package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.nio.file.FileStore;

public interface EditModuleSetPage extends Page {
    void setName(String name);

    void setDescription(String description);

    WebElement getNameField();

    WebElement getDescriptionField();

    void hitUpdateButton();

    WebElement getUpdateButton(boolean enabled);

    void addModule();

    void addNewModuleFile();

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
