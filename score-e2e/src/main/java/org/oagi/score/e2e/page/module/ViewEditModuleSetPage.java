package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit Module Set' page.
 */
public interface ViewEditModuleSetPage extends Page {
    WebElement getNewModuleSetButton();

    EditModuleSetPage hitNewModuleSetButton();
}
