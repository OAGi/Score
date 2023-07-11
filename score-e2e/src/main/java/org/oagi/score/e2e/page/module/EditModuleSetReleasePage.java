package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.io.File;

public interface EditModuleSetReleasePage extends Page {
    void setName(String moduleSetReleaseName);

    WebElement getNameField();

    void setDescription(String description);

    WebElement getDescriptionField();

    void hitUpdateButton();

    WebElement getUpdateButton(boolean enabled);

    File hitExportButton();

    WebElement getExportButton();

    WebElement getValidateButton();

    ModuleSetReleaseXMLSchemaValidationDialog hitValidateButton();
}
