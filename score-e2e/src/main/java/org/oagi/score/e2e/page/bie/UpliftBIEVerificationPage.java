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

    /**
     * Click the verification 'Next' button (which opens the 'Uplift BIE Report'
     * dialog) and then the report dialog's 'Uplift' button, without waiting for
     * the uplift to complete.
     * <p>
     * When a mapped reuse node was left without a target BIE selected, this
     * surfaces the 'Proceed without selecting reuse BIEs?' confirmation
     * (OAGi/Score#1735) instead of finishing the uplift.
     */
    void submitUpliftReport();

    /**
     * Return the header element of the unselected-reuse confirmation dialog
     * ('Proceed without selecting reuse BIEs?') shown when one or more mapped
     * reuse nodes were left without a target BIE selected (OAGi/Score#1735).
     *
     * @return the header element of the unselected-reuse confirmation dialog
     */
    WebElement getUnselectedReuseWarning();

    /**
     * Confirm the unselected-reuse warning by clicking 'Continue' and return the
     * resulting uplifted BIE edit page (OAGi/Score#1735).
     *
     * @return the uplifted BIE edit page
     */
    EditBIEPage confirmUnselectedReuseAndUplift();

}
