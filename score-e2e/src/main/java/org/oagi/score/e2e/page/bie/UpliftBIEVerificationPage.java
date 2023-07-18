package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface UpliftBIEVerificationPage extends Page {

    /**
     * Return the UI element of the 'Next' button in the paginator.
     *
     * @return the UI element of the 'Next' button in the paginator
     */
    WebElement getNextButton();
}
