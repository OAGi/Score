package org.oagi.score.e2e.page.business_term;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface for 'View/Edit Business Term' page
 */
public interface ViewEditBusinessTermPage extends Page {
    /**
     * Return the UI element of the 'New Business Term' button.
     *
     * @return the UI element of the 'New Business Term' button
     */
    WebElement getNewBusinessTermButton();

    /**
     * Hit the 'New Business Term' button
     */
    void hitNewBusinessTermButton();
}
