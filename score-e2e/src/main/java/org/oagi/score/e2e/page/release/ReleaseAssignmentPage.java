package org.oagi.score.e2e.page.release;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface ReleaseAssignmentPage extends Page {
    WebElement getDENField();

    void setDEN(String den);

    WebElement getTypeSelectField();

    void setType(String type);

    WebElement getOwnerSelectField();

    void setOwner(String owner);

    WebElement getSearchButton();

    void hitSearchButton();

    WebElement getCreateButton();

    void hitCreateButton();

    WebElement getAssignAllButton();

    void hitAssignAllButton();

    WebElement getValidateButton();

    void hitValidateButton();
}
