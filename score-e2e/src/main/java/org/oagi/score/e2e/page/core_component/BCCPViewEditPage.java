package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit BCCP' page.
 */
public interface BCCPViewEditPage extends Page {

    /**
     * Return the UI element of the 'Core Component' field.
     *
     * @return the UI element of the 'Core Component' field
     */
    WebElement getCoreComponentTypeField();

    /**
     * Return the value of the 'Core Component' field.
     *
     * @return the value of the 'Core Component' field
     */
    String getCoreComponentTypeFieldValue();

    /**
     * Return the UI element of the 'Release' field.
     *
     * @return the UI element of the 'Release' field
     */
    WebElement getReleaseField();

    /**
     * Return the value of the 'Release' field.
     *
     * @return the value of the 'Release' field
     */
    String getReleaseFieldValue();

    /**
     * Return the UI element of the 'Revision' field.
     *
     * @return the UI element of the 'Revision' field
     */
    WebElement getRevisionField();

    /**
     * Return the value of the 'Revision' field.
     *
     * @return the value of the 'Revision' field
     */
    String getRevisionFieldValue();

    /**
     * Return the UI element of the 'State' field.
     *
     * @return the UI element of the 'State' field
     */
    WebElement getStateField();

    /**
     * Return the value of the 'State' field.
     *
     * @return the value of the 'State' field
     */
    String getStateFieldValue();

    /**
     * Return the UI element of the 'Owner' field.
     *
     * @return the UI element of the 'Owner' field
     */
    WebElement getOwnerField();

    /**
     * Return the value of the 'Owner' field.
     *
     * @return the value of the 'Owner' field
     */
    String getOwnerFieldValue();

    /**
     * Return the UI element of the 'GUID' field.
     *
     * @return the UI element of the 'GUID' field
     */
    WebElement getGUIDField();

    /**
     * Return the value of the 'GUID' field.
     *
     * @return the value of the 'GUID' field
     */
    String getGUIDFieldValue();

    /**
     * Return the UI element of the 'DEN' field.
     *
     * @return the UI element of the 'DEN' field
     */
    WebElement getDENField();

    /**
     * Return the value of the 'DEN' field.
     *
     * @return the value of the 'DEN' field
     */
    String getDENFieldValue();

    /**
     * Return the UI element of the 'Property Term' field.
     *
     * @return the UI element of the 'Property Term' field
     */
    WebElement getPropertyTermField();

    /**
     * Return the 'Property Term' field label text.
     *
     * @return the 'Property Term' field label text
     */
    String getPropertyTermFieldLabel();

    /**
     * Return the value of the 'Property Term' field.
     *
     * @return the value of the 'Property Term' field
     */
    String getPropertyTermFieldValue();

    /**
     * Return the UI element of the 'Namespace' field.
     *
     * @return the UI element of the 'Namespace' field
     */
    WebElement getNamespaceField();

    /**
     * Return the value of the 'Namespace' field.
     *
     * @return the value of the 'Namespace' field
     */
    String getNamespaceFieldValue();

    /**
     * Return the UI element of the 'Definition Source' field.
     *
     * @return the UI element of the 'Definition Source' field
     */
    WebElement getDefinitionSourceField();

    /**
     * Return the value of the 'Definition Source' field.
     *
     * @return the value of the 'Definition Source' field
     */
    String getDefinitionSourceFieldValue();

    /**
     * Return the UI element of the 'Definition' field.
     *
     * @return the UI element of the 'Definition' field
     */
    WebElement getDefinitionField();

    /**
     * Return the value of the 'Definition' field.
     *
     * @return the value of the 'Definition' field
     */
    String getDefinitionFieldValue();

    String getDENFieldLabel();

    WebElement getPropertyTermComponent();

}
