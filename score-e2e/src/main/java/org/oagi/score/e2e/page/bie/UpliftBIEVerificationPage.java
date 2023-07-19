package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface UpliftBIEVerificationPage extends Page {

    void expandNodeInSourceBIE(String node);

    void expandNodeInTargetBIE(String node);

    WebElement goToNodeInSourceBIE(String nodePath);

    WebElement goToNodeInTargetBIE(String nodePath);

    WebElement getSearchInputOfSourceTree();

    WebElement getSearchInputOfTargetTree();

    /**
     * Return the UI element of the 'Next' button in the paginator.
     *
     * @return the UI element of the 'Next' button in the paginator
     */
    WebElement getNextButton();

    void next();
}
