package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface UpliftBIEReportPage extends Page {
    /**
     * Return the UI element of the 'Uplift' button in the paginator.
     *
     * @return the UI element of the 'Uplift' button in the paginator
     */
    WebElement getUpliftButton();

    /**
     * Return the UI element of the 'Cancel' button in the paginator.
     *
     * @return the UI element of the 'Cancel' button in the paginator
     */
    WebElement getCancelButton();

    /**
     * Return the UI element of the 'Download' button in the paginator.
     *
     * @return the UI element of the 'Download' button in the paginator
     */
    WebElement getDownloadButton();
}
