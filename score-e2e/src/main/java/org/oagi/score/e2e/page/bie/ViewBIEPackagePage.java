package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'BIE Package' page.
 */
public interface ViewBIEPackagePage extends Page {

    WebElement getNewBIEPackageButton();

    /**
     * Click 'New BIE Package' and return the detail page object for the newly created (WIP) package.
     */
    EditBIEPackagePage hitNewBIEPackageButton();
}
