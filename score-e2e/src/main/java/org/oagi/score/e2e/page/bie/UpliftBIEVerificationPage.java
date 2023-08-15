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

    WebElement getCheckBoxOfNodeInTargetBIE(String node);

    SelectProfileBIEToReuseDialog reuseBIEOnNode(String path, String nodeName);

    /**
     * Return the UI element of the 'Reused' icon of the node in the target BIE.
     *
     * @return the UI element of the 'Reused' icon of the node in the target BIE
     */
    WebElement getReusedIconOfNodeInTargetBIE(String nodeName);

    /**
     * Return the UI element of the 'Next' button.
     *
     * @return the UI element of the 'Next' button
     */
    WebElement getNextButton();

    /**
     * Uplift the BIE.
     */
    EditBIEPage uplift();

}
