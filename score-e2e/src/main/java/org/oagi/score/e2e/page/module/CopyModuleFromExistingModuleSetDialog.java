package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface CopyModuleFromExistingModuleSetDialog extends Dialog {
    void setModuleSet(String moduleSetName);

    WebElement getModuleSetSelectField();

    void selectModule(String moduleName);

    WebElement getModuleByName(String moduleName);

    void copyModule();

    void toggleCopyAllSubmodules();

    WebElement getCopyAllSubmodulesSelectField();
}
