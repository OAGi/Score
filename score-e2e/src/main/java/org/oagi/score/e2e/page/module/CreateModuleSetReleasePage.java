package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface CreateModuleSetReleasePage extends Page {
    void setName(String moduleSetReleaseName);

    WebElement getNameField();

    void setDescription(String description);

    WebElement getDescriptionField();

    void setModuleSet(String name);

    WebElement getModuleSetSelectField();

    void setRelease(String releaseNumber);

    WebElement getReleaseSelectField();

    void hitCreateButton();

    WebElement getCreateButton();

    void toggleDefault();

    WebElement getDefaultSelectField();
}
