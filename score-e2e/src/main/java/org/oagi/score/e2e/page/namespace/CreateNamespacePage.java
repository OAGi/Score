package org.oagi.score.e2e.page.namespace;

import org.openqa.selenium.WebElement;

public interface CreateNamespacePage {
    WebElement getURIField();

    void setURI(String uri);

    WebElement getPrefixField();

    void setPrefix(String prefix);

    WebElement getDescriptionField();

    void setDescription(String description);

    WebElement getStandardCheckboxField();

    void hitCreateButton();

    WebElement getCreateButton();
    void hitBackButton();

    WebElement getBackButton();

    boolean isOpened();

    void openPage();
}
