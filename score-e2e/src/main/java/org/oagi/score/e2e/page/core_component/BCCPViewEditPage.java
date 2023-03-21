package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit BCCP' page.
 */
public interface BCCPViewEditPage extends Page {

    /**
     * Return the UI element of the input text field for searching nodes.
     *
     * @return the UI element of the input text field for searching nodes
     */
    WebElement getSearchInputTextField();

    /**
     * Return the UI element of the 'Search' button.
     *
     * @return the UI element of the 'Search' button
     */
    WebElement getSearchButton();

    /**
     * Return the UI element of the tree node by the node path.
     *
     * @param path the node path
     * @return the UI element of the tree node
     */
    WebElement getNodeByPath(String path);

    /**
     * Return the UI element of the 'Context Menu' icon for the node.
     *
     * @param nodeName Node name
     * @return the UI element of the 'Context Menu' icon
     */
    WebElement getContextMenuIconByNodeName(String nodeName);

    /**
     * Click the drop-down menu to open the context menu on the node.
     *
     * @param path the path of the node
     * @return node UI element
     */
    WebElement clickOnDropDownMenuByPath(String path);

    /**
     * Open the 'Change BDT' dialog.
     *
     * @return the 'Change BDT' dialog object
     */
    BCCPChangeBDTDialog openChangeBDTDialog();

    /**
     * Return the UI element of the 'Revise' button. Developers only can see the 'Revise' button.
     *
     * @return the UI element of the 'Revise' button
     */
    WebElement getReviseButton();

    /**
     * Hit the 'Revise' button.
     */
    void hitReviseButton();

    /**
     * Return the UI element of the 'Amend' button. End-users only can see the 'Revise' button.
     *
     * @return the UI element of the 'Amend' button
     */
    WebElement getAmendButton();

    /**
     * Hit the 'Amend' button.
     */
    void hitAmendButton();

    /**
     * Return the UI element of the 'Move to QA' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Move to QA' button
     */
    WebElement getMoveToQAButton(boolean enabled);

    /**
     * Make the component to the QA state. It works only if the component is in the WIP state and the 'Update' button is disabled.
     *
     * @throws org.openqa.selenium.TimeoutException if the component is not in the WIP state or the 'Update' button is enabled.
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
     * Make the component back to the WIP state. It works only if the component is in the QA state.
     *
     * @throws org.openqa.selenium.TimeoutException if the component is not in the QA state.
     */
    void backToWIP();

    /**
     * Return the UI element of the 'Move to Production' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Move to Production' button
     */
    WebElement getMoveToProduction(boolean enabled);

    /**
     * Make the component to the Production state. It works only if the component is in the QA state.
     *
     * @throws org.openqa.selenium.TimeoutException if the component is not in the QA state.
     */
    void moveToProduction();

    /**
     * Return the UI element of the 'Move to Draft' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Move to Draft' button
     */
    WebElement getMoveToDraft(boolean enabled);

    /**
     * Make the component to the Draft state. It works only if the component is in the WIP state and the 'Update' button is disabled.
     *
     * @throws org.openqa.selenium.TimeoutException if the component is not in the WIP state or the 'Update' button is enabled.
     */
    void moveToDraft();

    /**
     * Return the UI element of the 'Move to Candidate' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Move to Candidate' button
     */
    WebElement getMoveToCandidate(boolean enabled);

    /**
     * Make the component to the Candidate state. It works only if the component is in the Draft state and the 'Update' button is disabled.
     *
     * @throws org.openqa.selenium.TimeoutException if the component is not in the Draft state or the 'Update' button is enabled.
     */
    void moveToCandidate();

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
     * Return the UI element of the 'Cancel' button, which displays only if it is revised/amended.
     *
     * @return the UI element of the 'Cancel' button
     */
    WebElement getCancelButton();

    /**
     * Hit the 'Cancel' button.
     */
    void hitCancelButton();

    /**
     * Return the UI element of the 'Delete' button, which displays only if it is in 'WIP' state.
     *
     * @return the UI element of the 'Delete' button
     */
    WebElement getDeleteButton();

    /**
     * Hit the 'Cancel' button.
     */
    void hitDeleteButton();

    /**
     * Return the UI element of the 'Restore' button, which displays only if it is in 'Deleted' state.
     *
     * @return the UI element of the 'Restore' button
     */
    WebElement getRestoreButton();

    /**
     * Hit the 'Restore' button.
     */
    void hitRestoreButton();

    /**
     * Return the BCCP panel container. Use this when the page is opened.
     *
     * @return the BCCP panel container
     */
    BCCPPanelContainer getBCCPPanelContainer();

    /**
     * Return the BCCP panel container.
     *
     * @param bccpNode BCCP node
     * @return the BCCP panel container
     */
    BCCPPanelContainer getBCCPPanelContainer(WebElement bccpNode);

    /**
     * Return the ACC panel.
     *
     * @param accNode ACC node
     * @return the ACC panel
     */
    ACCPanel getACCPanel(WebElement accNode);

    /**
     * Return the ASCC panel container.
     *
     * @param asccNode ASCC node
     * @return the ASCC panel container
     */
    ASCCPanelContainer getASCCPanelContainer(WebElement asccNode);

    /**
     * Return the BCC panel container.
     *
     * @param bccNode BCC node
     * @return the BCC panel container
     */
    BCCPanelContainer getBCCPanelContainer(WebElement bccNode);

    void setPropertyTerm(String propertyTerm);

    WebElement getPropertyTermField();

    void setNamespace(String namespace);

    WebElement getNamespaceSelectField();

    void hitUpdateAnywayButton();

    WebElement getUpdateAnywayButton();

    WebElement getDefinitionField();

    void setDefinition(String definition);

    /**
     * An interface of the ACC panel
     */
    interface ACCPanel {

        /**
         * Return the UI element of the 'Core Component' field.
         *
         * @return the UI element of the 'Core Component' field
         */
        WebElement getCoreComponentField();

        /**
         * Return the UI element of the 'Release' field.
         *
         * @return the UI element of the 'Release' field
         */
        WebElement getReleaseField();

        /**
         * Return the UI element of the 'Revision' field.
         *
         * @return the UI element of the 'Revision' field
         */
        WebElement getRevisionField();

        /**
         * Return the UI element of the 'State' field.
         *
         * @return the UI element of the 'State' field
         */
        WebElement getStateField();

        /**
         * Return the UI element of the 'Owner' field.
         *
         * @return the UI element of the 'Owner' field
         */
        WebElement getOwnerField();

        /**
         * Return the UI element of the 'GUID' field.
         *
         * @return the UI element of the 'GUID' field
         */
        WebElement getGUIDField();

        /**
         * Return the UI element of the 'DEN' field.
         *
         * @return the UI element of the 'DEN' field
         */
        WebElement getDENField();

        /**
         * Return the UI element of the 'Object Class Term' field.
         *
         * @return the UI element of the 'Object Class Term' field
         */
        WebElement getObjectClassTermField();

        /**
         * Return the UI element of the 'Component Type' select field.
         *
         * @return the UI element of the 'Component Type' select field
         */
        WebElement getComponentTypeSelectField();

        /**
         * Return the UI element of the 'Abstract' checkbox.
         *
         * @return the UI element of the 'Abstract' checkbox
         */
        WebElement getAbstractCheckbox();

        /**
         * Return the UI element of the 'Deprecated' checkbox.
         *
         * @return the UI element of the 'Deprecated' checkbox
         */
        WebElement getDeprecatedCheckbox();

        /**
         * Return the UI element of the 'Namespace' select field.
         *
         * @return the UI element of the 'Namespace' select field
         */
        WebElement getNamespaceSelectField();

        /**
         * Return the UI element of the 'Definition Source' field.
         *
         * @return the UI element of the 'Definition Source' field
         */
        WebElement getDefinitionSourceField();

        /**
         * Return the UI element of the 'Definition' field.
         *
         * @return the UI element of the 'Definition' field
         */
        WebElement getDefinitionField();

    }

    interface ASCCPanelContainer {

        ASCCPanel getASCCPanel();

        ASCCPPanel getASCCPPanel();

    }

    /**
     * An interface of the ASCC panel
     */
    interface ASCCPanel {

        /**
         * Return the UI element of the 'Core Component' field.
         *
         * @return the UI element of the 'Core Component' field
         */
        WebElement getCoreComponentField();

        /**
         * Return the UI element of the 'Release' field.
         *
         * @return the UI element of the 'Release' field
         */
        WebElement getReleaseField();

        /**
         * Return the UI element of the 'Revision' field.
         *
         * @return the UI element of the 'Revision' field
         */
        WebElement getRevisionField();

        /**
         * Return the UI element of the 'State' field.
         *
         * @return the UI element of the 'State' field
         */
        WebElement getStateField();

        /**
         * Return the UI element of the 'Owner' field.
         *
         * @return the UI element of the 'Owner' field
         */
        WebElement getOwnerField();

        /**
         * Return the UI element of the 'GUID' field.
         *
         * @return the UI element of the 'GUID' field
         */
        WebElement getGUIDField();

        /**
         * Return the UI element of the 'DEN' field.
         *
         * @return the UI element of the 'DEN' field
         */
        WebElement getDENField();

        /**
         * Return the UI element of the 'Cardinality Min' field.
         *
         * @return the UI element of the 'Cardinality Min' field
         */
        WebElement getCardinalityMinField();

        /**
         * Return the UI element of the 'Cardinality Max' field.
         *
         * @return the UI element of the 'Cardinality Max' field
         */
        WebElement getCardinalityMaxField();

        /**
         * Return the UI element of the 'Deprecated' checkbox.
         *
         * @return the UI element of the 'Deprecated' checkbox
         */
        WebElement getDeprecatedCheckbox();

        /**
         * Return the UI element of the 'Definition Source' field.
         *
         * @return the UI element of the 'Definition Source' field
         */
        WebElement getDefinitionSourceField();

        /**
         * Return the UI element of the 'Definition' field.
         *
         * @return the UI element of the 'Definition' field
         */
        WebElement getDefinitionField();

    }

    /**
     * An interface of the ASCCP panel
     */
    interface ASCCPPanel {

        /**
         * Return the UI element of the 'Core Component' field.
         *
         * @return the UI element of the 'Core Component' field
         */
        WebElement getCoreComponentField();

        /**
         * Return the UI element of the 'Release' field.
         *
         * @return the UI element of the 'Release' field
         */
        WebElement getReleaseField();

        /**
         * Return the UI element of the 'Revision' field.
         *
         * @return the UI element of the 'Revision' field
         */
        WebElement getRevisionField();

        /**
         * Return the UI element of the 'State' field.
         *
         * @return the UI element of the 'State' field
         */
        WebElement getStateField();

        /**
         * Return the UI element of the 'Owner' field.
         *
         * @return the UI element of the 'Owner' field
         */
        WebElement getOwnerField();

        /**
         * Return the UI element of the 'GUID' field.
         *
         * @return the UI element of the 'GUID' field
         */
        WebElement getGUIDField();

        /**
         * Return the UI element of the 'DEN' field.
         *
         * @return the UI element of the 'DEN' field
         */
        WebElement getDENField();

        /**
         * Return the label of the 'DEN' field.
         *
         * @return the label of the 'DEN' field
         */
        String getDENFieldLabel();

        /**
         * Return the UI element of the 'Property Term' field.
         *
         * @return the UI element of the 'Property Term' field
         */
        WebElement getPropertyTermField();

        /**
         * Return the label of the 'Property Term' field.
         *
         * @return the label of the 'Property Term' field
         */
        String getPropertyTermFieldLabel();

        /**
         * Return the UI element of the 'Reusable' checkbox.
         *
         * @return the UI element of the 'Reusable' checkbox
         */
        WebElement getReusableCheckbox();

        /**
         * Return the UI element of the 'Nillable' checkbox.
         *
         * @return the UI element of the 'Nillable' checkbox
         */
        WebElement getNillableCheckbox();

        /**
         * Return the UI element of the 'Deprecated' checkbox.
         *
         * @return the UI element of the 'Deprecated' checkbox
         */
        WebElement getDeprecatedCheckbox();

        /**
         * Return the UI element of the 'Namespace' select field.
         *
         * @return the UI element of the 'Namespace' select field
         */
        WebElement getNamespaceSelectField();

        /**
         * Return the UI element of the 'Definition Source' field.
         *
         * @return the UI element of the 'Definition Source' field
         */
        WebElement getDefinitionSourceField();

        /**
         * Return the UI element of the 'Definition' field.
         *
         * @return the UI element of the 'Definition' field
         */
        WebElement getDefinitionField();

    }

    interface BCCPanelContainer {

        BCCPanel getBCCPanel();

        BCCPPanel getBCCPPanel();

        DTPanel getDTPanel();

    }

    /**
     * An interface of the BCC panel
     */
    interface BCCPanel {

        /**
         * Return the UI element of the 'Core Component' field.
         *
         * @return the UI element of the 'Core Component' field
         */
        WebElement getCoreComponentField();

        /**
         * Return the UI element of the 'Release' field.
         *
         * @return the UI element of the 'Release' field
         */
        WebElement getReleaseField();

        /**
         * Return the UI element of the 'Revision' field.
         *
         * @return the UI element of the 'Revision' field
         */
        WebElement getRevisionField();

        /**
         * Return the UI element of the 'State' field.
         *
         * @return the UI element of the 'State' field
         */
        WebElement getStateField();

        /**
         * Return the UI element of the 'Owner' field.
         *
         * @return the UI element of the 'Owner' field
         */
        WebElement getOwnerField();

        /**
         * Return the UI element of the 'GUID' field.
         *
         * @return the UI element of the 'GUID' field
         */
        WebElement getGUIDField();

        /**
         * Return the UI element of the 'DEN' field.
         *
         * @return the UI element of the 'DEN' field
         */
        WebElement getDENField();

        /**
         * Return the UI element of the 'Cardinality Min' field.
         *
         * @return the UI element of the 'Cardinality Min' field
         */
        WebElement getCardinalityMinField();

        /**
         * Return the UI element of the 'Cardinality Max' field.
         *
         * @return the UI element of the 'Cardinality Max' field
         */
        WebElement getCardinalityMaxField();

        /**
         * Return the UI element of the 'Entity Type' select field.
         *
         * @return the UI element of the 'Entity Type' select field
         */
        WebElement getEntityTypeSelectField();

        /**
         * Return the UI element of the 'Deprecated' checkbox.
         *
         * @return the UI element of the 'Deprecated' checkbox
         */
        WebElement getDeprecatedCheckbox();

        /**
         * Return the UI element of the 'Value Constraint' select field.
         *
         * @return the UI element of the 'Value Constraint' select field
         */
        WebElement getValueConstraintSelectField();

        /**
         * Return the UI element of the 'Fixed Value' field.
         *
         * @return the UI element of the 'Fixed Value' field
         */
        WebElement getFixedValueField();

        /**
         * Return the UI element of the 'Default Value' field.
         *
         * @return the UI element of the 'Default Value' field
         */
        WebElement getDefaultValueField();

        /**
         * Return the UI element of the 'Definition Source' field.
         *
         * @return the UI element of the 'Definition Source' field
         */
        WebElement getDefinitionSourceField();

        /**
         * Return the UI element of the 'Definition' field.
         *
         * @return the UI element of the 'Definition' field
         */
        WebElement getDefinitionField();

    }

    interface BCCPPanelContainer {

        BCCPPanel getBCCPPanel();

        DTPanel getDTPanel();

    }

    /**
     * An interface of the BCCP panel
     */
    interface BCCPPanel {

        /**
         * Return the UI element of the 'Core Component' field.
         *
         * @return the UI element of the 'Core Component' field
         */
        WebElement getCoreComponentField();

        /**
         * Return the UI element of the 'Release' field.
         *
         * @return the UI element of the 'Release' field
         */
        WebElement getReleaseField();

        /**
         * Return the UI element of the 'Revision' field.
         *
         * @return the UI element of the 'Revision' field
         */
        WebElement getRevisionField();

        /**
         * Return the UI element of the 'State' field.
         *
         * @return the UI element of the 'State' field
         */
        WebElement getStateField();

        /**
         * Return the UI element of the 'Owner' field.
         *
         * @return the UI element of the 'Owner' field
         */
        WebElement getOwnerField();

        /**
         * Return the UI element of the 'GUID' field.
         *
         * @return the UI element of the 'GUID' field
         */
        WebElement getGUIDField();

        /**
         * Return the UI element of the 'DEN' field.
         *
         * @return the UI element of the 'DEN' field
         */
        WebElement getDENField();

        /**
         * Return the UI element of the 'Property Term' field.
         *
         * @return the UI element of the 'Property Term' field
         */
        WebElement getPropertyTermField();

        /**
         *
         * @param propertyTerm
         */
        void setPropertyTerm(String propertyTerm);

        /**
         * Return the label of the 'Property Term' field.
         *
         * @return the label of the 'Property Term' field
         */
        String getPropertyTermFieldLabel();

        /**
         * Return the UI element of the 'Nillable' checkbox.
         *
         * @return the UI element of the 'Nillable' checkbox
         */
        WebElement getNillableCheckbox();

        /**
         * Toggle the 'Nillable' checkbox
         */
        void toggleNillable();

        /**
         * Return the UI element of the 'Value Constraint' select field.
         *
         * @return the UI element of the 'Value Constraint' select field
         */
        WebElement getValueConstraintSelectField();

        /**
         *
         * @param valueConstraint
         */
        void setValueConstraint(String valueConstraint);

        /**
         * Return the UI element of the 'Fixed Value' field.
         *
         * @return the UI element of the 'Fixed Value' field
         */
        WebElement getFixedValueField();

        /**
         *
         * @param fixedValue
         */
        void setFixedValue(String fixedValue);

        /**
         * Return the UI element of the 'Default Value' field.
         *
         * @return the UI element of the 'Default Value' field
         */
        WebElement getDefaultValueField();

        /**
         *
         * @param defaultValue
         */
        void setDefaultValue(String defaultValue);

        /**
         * Return the UI element of the 'Deprecated' checkbox.
         *
         * @return the UI element of the 'Deprecated' checkbox
         */
        WebElement getDeprecatedCheckbox();

        /**
         * Return the UI element of the 'Namespace' select field.
         *
         * @return the UI element of the 'Namespace' select field
         */
        WebElement getNamespaceSelectField();

        /**
         * Set the 'Namespace' field with the given text.
         *
         * @param namespace Namespace
         */
        void setNamespace(String namespace);

        /**
         * Return the UI element of the 'Definition Source' field.
         *
         * @return the UI element of the 'Definition Source' field
         */
        WebElement getDefinitionSourceField();

        /**
         * Set the 'Definition Source' field with the given text.
         *
         * @param definitionSource Definition Source
         */
        void setDefinitionSource(String definitionSource);

        /**
         * Return the UI element of the 'Definition' field.
         *
         * @return the UI element of the 'Definition' field
         */
        WebElement getDefinitionField();

        /**
         * Set the 'Definition' field with the given text.
         *
         * @param definition Definition
         */
        void setDefinition(String definition);

    }

    /**
     * An interface of the DT panel
     */
    interface DTPanel {

        /**
         * Return the UI element of the 'Core Component' field.
         *
         * @return the UI element of the 'Core Component' field
         */
        WebElement getCoreComponentField();

        /**
         * Return the UI element of the 'Release' field.
         *
         * @return the UI element of the 'Release' field
         */
        WebElement getReleaseField();

        /**
         * Return the UI element of the 'Revision' field.
         *
         * @return the UI element of the 'Revision' field
         */
        WebElement getRevisionField();

        /**
         * Return the UI element of the 'State' field.
         *
         * @return the UI element of the 'State' field
         */
        WebElement getStateField();

        /**
         * Return the UI element of the 'Owner' field.
         *
         * @return the UI element of the 'Owner' field
         */
        WebElement getOwnerField();

        /**
         * Return the UI element of the 'GUID' field.
         *
         * @return the UI element of the 'GUID' field
         */
        WebElement getGUIDField();

        /**
         * Return the UI element of the 'DEN' field.
         *
         * @return the UI element of the 'DEN' field
         */
        WebElement getDENField();

        /**
         * Return the UI element of the 'Data Type Term' field.
         *
         * @return the UI element of the 'Data Type Term' field
         */
        WebElement getDataTypeTermField();

        /**
         * Return the UI element of the 'Qualifier' field.
         *
         * @return the UI element of the 'Qualifier' field
         */
        WebElement getQualifierField();

        /**
         * Return the UI element of the 'Definition Source' field.
         *
         * @return the UI element of the 'Definition Source' field
         */
        WebElement getDefinitionSourceField();

        /**
         * Return the UI element of the 'Definition' field.
         *
         * @return the UI element of the 'Definition' field
         */
        WebElement getDefinitionField();

    }

}
