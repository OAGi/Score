package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface EditModuleSetPage extends Page {
    WebElement getNameField();

    WebElement getDescriptionField();

    void toggleCreateModuleSetRelease();

    WebElement getCreateModuleSetReleaseSelectField();

    void setRelease(String releaseNumber);

    WebElement getReleaseSelectField();

    void setModuleSetRelease(String moduleSetRelease);

    WebElement getModuleSetReleaseSelectField();

    void hitCreateButton();

    WebElement getCreateButton();

    void setName(String name);

    void setDescription(String description);
}
