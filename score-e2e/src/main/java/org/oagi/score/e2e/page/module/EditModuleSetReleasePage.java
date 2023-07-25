package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.obj.ModuleSetReleaseObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.io.File;

public interface EditModuleSetReleasePage extends Page {

    void setName(String moduleSetReleaseName);

    WebElement getNameField();

    void setDescription(String description);

    WebElement getDescriptionField();

    /**
     * Return the UI element of the 'Module Set' select field.
     *
     * @return the UI element of the 'Module Set' select field
     */
    WebElement getModuleSetSelectField();

    /**
     * Return the UI element of the 'Release' select field.
     *
     * @return the UI element of the 'Release' select field
     */
    WebElement getReleaseSelectField();

    /**
     * Return the UI element of the 'Default' checkbox.
     *
     * @return the UI element of the 'Default' checkbox
     */
    WebElement getDefaultCheckbox();

    void toggleDefault();

    void hitUpdateButton();

    WebElement getUpdateButton(boolean enabled);

    File hitExportButton();

    WebElement getExportButton();

    WebElement getValidateButton();

    WebElement getAssignCCsbutton();

    ModuleSetReleaseXMLSchemaValidationDialog hitValidateButton();

    CoreComponentAssignmentPage hitAssignCCsButton(ModuleSetReleaseObject moduleSetRelease);

    CoreComponentAssignmentPage viewAssignedCCs(ModuleSetReleaseObject latestModuleSetRelease);

    WebElement getViewAssignedCCsbutton();
}
