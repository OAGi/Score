package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface ACCExtensionViewEditPage extends Page {

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
     * Return the UI element of the 'Object Class Term' field.
     *
     * @return the UI element of the 'Object Class Term' field
     */
    WebElement getObjectClassTermField();

    /**
     * Return the 'Object Class Term' field label text.
     *
     * @return the 'Object Class Term' field label text
     */
    String getObjectClassTermFieldLabel();

    String getDENFieldLabel();

    /**
     * Return the value of the 'Object Class Term' field.
     *
     * @return the value of the 'Object Class Term' field
     */
    String getObjectClassTermFieldValue();

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
     * Set the namespace to the 'Namespace' field.
     *
     * @param namespace Namespace
     */
    void setNamespace(NamespaceObject namespace);

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

    /**
     * Return the UI element of the 'Context Menu' icon for the node.
     *
     * @param nodeName Node name
     * @return the UI element of the 'Context Menu' icon
     */
    WebElement getContextMenuIconByNodeName(String nodeName);

    /**
     * Open the association selection dialog by clicking 'Append Property at Last' context menu.
     *
     * @param path node path
     * @return the association selection dialog object
     */
    SelectAssociationDialog appendPropertyAtLast(String path);

    /**
     * Click the drop-down menu to open the context menu on the node.
     *
     * @param path the path of the node
     * @return node UI element
     */
    WebElement clickOnDropDownMenuByPath(String path);

    WebElement getSearchField();

    /**
     * Return the UI element of the 'Update' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Update' button
     */
    WebElement getUpdateButton(boolean enabled);

    /**
     * Hit the 'Update' button.
     *
     * @throws AssertionError if updating fails.
     */
    void hitUpdateButton();

    /**
     * Return the UI element of the 'Move to QA' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Move to QA' button
     */
    WebElement getMoveToQAButton(boolean enabled);

    /**
     * Make the ACC to the QA state. It works only if the ACC is in the WIP state and the 'Update' button is disabled.
     *
     * @throws org.openqa.selenium.TimeoutException if the ACC is not in the WIP state or the 'Update' button is enabled.
     */
    void moveToQA();

    /**
     * Return the UI element of the 'Back to WIP' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Back to WIP' button
     */
    WebElement getBackToWIPButton(boolean enabled);

    /**
     * Make the ACC back to the WIP state. It works only if the ACC is in the Draft/QA state.
     *
     * @throws org.openqa.selenium.TimeoutException if the ACC is not in the Draft/QA state.
     */
    void backToWIP();

    /**
     * Return the UI element of the 'Move to Production' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Move to Production' button
     */
    WebElement getMoveToProductionButton(boolean enabled);

    /**
     * Make the ACC to the Production state. It works only if the ACC is in the QA state.
     *
     * @throws org.openqa.selenium.TimeoutException if the ACC is not in the QA state.
     */
    void moveToProduction();

    /**
     * Return the UI element of the 'Amend' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Amend' button
     */
    WebElement getAmendButton(boolean enabled);

}
