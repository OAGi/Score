package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface EditModuleSetPage extends Page {
    void setName(String name);

    void setDescription(String description);

    WebElement getNameField();

    WebElement getDescriptionField();

    void hitUpdateButton();

    WebElement getUpdateButton(boolean enabled);
}
