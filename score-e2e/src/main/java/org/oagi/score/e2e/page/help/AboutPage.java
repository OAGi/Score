package org.oagi.score.e2e.page.help;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * An interface of 'About' page.
 */
public interface AboutPage extends Page {

    /**
     * Return the list of product versions.
     *
     * @return the list of product versions
     */
    VersionList getVersionList();

    /**
     * The list of product versions.
     */
    interface VersionList {

        /**
         * Return the list of the product UI elements.
         *
         * @return the list of the product UI elements
         */
        List<WebElement> getItems();

        /**
         * Return the UI element of the product by the given name.
         *
         * @param name product name
         * @return the UI element of the product
         */
        WebElement getItemByName(String name);

    }

    /**
     * Return the UI element of the 'Contributors' link.
     *
     * @return the UI element of the 'Contributors' link
     */
    WebElement getContributorsLink();

    /**
     * Return the UI element of the License text.
     *
     * @return the UI element of the License text
     */
    WebElement getLicense();

}
