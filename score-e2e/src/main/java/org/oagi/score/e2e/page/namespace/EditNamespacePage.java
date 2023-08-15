package org.oagi.score.e2e.page.namespace;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface EditNamespacePage extends Page {
    WebElement getURIField();

    void setURI(String uri);

    WebElement getPrefixField();

    void setPrefix(String prefix);

    WebElement getDescriptionField();

    void setDescription(String description);

    WebElement getStandardCheckboxField();

    void hitUpdateButton();

    WebElement getUpdateButton();

    void hitDiscardButton();

    WebElement getDiscardButton();

    void hitBackButton();

    WebElement getBackButton();
}
