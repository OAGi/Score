package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface UpliftBIEVerificationPage extends Page {

    void expandNodeInSourceBIE(String node);

    void expandNodeInTargetBIE(String node);

    WebElement goToNodeInSourceBIE(String nodePath);

    /**
     * Return the UI element of the tree node by the node path.
     *
     * @param path the node path
     * @return the UI element of the tree node
     */
    WebElement goToNodeByPath(String path);

    WebElement goToNodeInTargetBIE(String nodePath);

    WebElement getSearchInputOfSourceTree();

    WebElement getSearchInputOfTargetTree();

    WebElement getCheckBoxOfNodeInTargetBIE(String node);
    SelectProfileBIEToReuseDialog reuseBIEOnNode(String path, String nodeName);
    /**
     * Return the UI element of the 'Next' button in the paginator.
     *
     * @return the UI element of the 'Next' button in the paginator
     */
    WebElement getReusedIconOfNodeInTargetBIE(String nodeName);
    WebElement getNextButton();

    void next();
}
