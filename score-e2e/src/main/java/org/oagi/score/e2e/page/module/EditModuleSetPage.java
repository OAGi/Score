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

    CreateModuleFileDialog addNewModuleFile();


    EditModuleFileDialog editModuleFile(String moduleFileName);

    WebElement getModuleEditLink(String moduleName);

    WebElement getModuleByName(String moduleName);

    CreateModuleDirectoryDialog addNewModuleDirectory();

    EditModuleDirectoryDialog editModuleDirectory(String moduleDirectoryName);

    CopyModuleFromExistingModuleSetDialog copyFromExistingModuleSet();
}
