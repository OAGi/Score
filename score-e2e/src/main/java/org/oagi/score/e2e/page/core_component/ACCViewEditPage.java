package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit ACC' page.
 */
public interface ACCViewEditPage extends Page {

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
     * @param objectClassTerm
     */
    void setObjectClassTerm(String objectClassTerm);

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
     * Open the association selection dialog by clicking 'Set Base ACC' context menu.
     *
     * @param path node path
     * @return the association selection dialog object
     */
    ACCSetBaseACCDialog setBaseACC(String path);

    /**
     * remove the base ACC by clicking 'Delete' context menu.
     *
     * @param path node path
     */
    void deleteBaseACC(String path);

    /**
     * Open the 'Create Extension Component' dialog by clicking 'Create OAGi Extension Component' context menu.
     *
     * @param path node path
     * @return the ACCViewEditPage
     */
    ACCViewEditPage createOAGiExtensionComponent(String path);

    /**
     * Open the association selection dialog by clicking 'Append Property at Last' context menu.
     *
     * @param path node path
     * @return the association selection dialog object
     */
    SelectAssociationDialog appendPropertyAtLast(String path);

    /**
     * Open the association selection dialog by clicking 'Where Used' context menu.
     *
     * @param path node path
     * @return the find 'Where used' dialog object
     */
    FindWhereUsedDialog findWhereUsed(String path);

    /**
     * Open the association selection dialog by clicking 'Create ASCCP from this' context menu.
     *
     * @param path node path
     * @return the 'Create ASCCP from this' dialog object
     */
    WebElement createASCCPfromThis(String path);

    /**
     * Open the BCCP page through 'Open in new tab' context menu for the given BCC node.
     *
     * @param bccNode BCC node
     * @return the BCCP page object
     */
    BCCPViewEditPage openBCCPInNewTab(WebElement bccNode);

    /**
     * Open the ASCCP page through 'Open in new tab' context menu for the given ACC node.
     *
     * @param accNode ACC node
     * @return the ASCCP page object
     */
    ASCCPViewEditPage openASCCPInNewTab(WebElement accNode);

    /**
     * Open the association selection dialog by clicking 'Insert Property Before' context menu.
     *
     * @param path node path
     * @return the association selection dialog object
     */
    SelectAssociationDialog insertPropertyBefore(String path);


    /**
     * Open the association selection dialog by clicking 'Insert Property After' context menu.
     *
     * @param path node path
     * @return the association selection dialog object
     */
    SelectAssociationDialog insertPropertyAfter(String path);

    /**
     * Open the association selection dialog by clicking 'Refactor/Refactor to Base' context menu.
     *
     * @param path
     * @param associationPropertyTerm ASCCP or BCCP propertyTerm
     * @return
     */
    SelectBaseACCToRefactorDialog refactorToBaseACC(String path, String associationPropertyTerm);

    /**
     * Ungroup the association by clicking 'Refactor/Ungroup' context menu
     *
     * @param path
     */
    void unGroup(String path);

    /**
     * Remove the association by clicking 'Remove' context menu.
     *
     * @param path node path
     * @return the ACCViewEditPage
     */
    ACCViewEditPage removeAssociation(String path);

    /**
     * Click the drop-down menu to open the context menu on the node.
     *
     * @param path the path of the node
     * @return node UI element
     */
    WebElement clickOnDropDownMenuByPath(String path);

    /**
     * Check the node whether it is in 'Deleted' or not.
     *
     * @param node node
     * @return {@code true} if the node is in 'Deleted', otherwise {@code false}
     */
    boolean isDeleted(WebElement node);

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
     * Return the UI element of the 'Cancel' button. Developers only can see the 'Cancel' button.
     *
     * @return the UI element of the 'Cancel' button
     */
    WebElement getCancelButton();

    /**
     * Hit the 'Cancel' button.
     */
    void hitCancelButton();

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
     * Return the History page of the current component in a new tab.
     *
     * @return the History page.
     */
    HistoryPage showHistory();

    AddCommentDialog openCommentsDialog(String path);

    /**
     * Hit the 'Update' button.
     */
    void hitUpdateButton();

    /**
     * Return the UI element of the 'Delete' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Delete' button
     */
    WebElement getDeleteButton(boolean enabled);

    /**
     *
     */
    void hitDeleteButton();

    /**
     * Return the UI element of the 'Restore' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Restore' button
     */
    WebElement getRestoreButton(boolean enabled);

    /**
     *
     */
    void hitRestoreButton();

    void expandTree(String nodeName);

    String getCardinalityLabel();

    String getDenFieldLabelForASCC();

    String getDenFieldLabelForBCC();

    String getDENFieldLabelDT();

    WebElement getCardinalityMaxField();

    void setCardinalityMax(int cardinalityMax);

    /**
     * Return the UI element of the tree node by the node path.
     *
     * @param path the node path
     * @return the UI element of the tree node
     */
    WebElement getNodeByPath(String path);

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
         * Set the "Object Class Term" field with given text.
         *
         * @param objectClassTerm
         */
        void setObjectClassTerm(String objectClassTerm);

        /**
         * Return the UI element of the 'Component Type' select field.
         *
         * @return the UI element of the 'Component Type' select field
         */
        WebElement getComponentTypeSelectField();

        /**
         * Set the 'Component' field with the given text.
         *
         * @param componentType "Base" "Semantic" or "Semantic Group"
         */
        void setComponentType(String componentType);

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
         * Set the 'Namespace' field with the given text.
         *
         * @param namespace Namespace
         */
        void setNamespace(String namespace);

        /**
         * Set the 'Namespace' field with the given namespace object.
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
         * set the UI element of the 'Cardinality Min' field by the given text
         *
         * @param cardinalityMin
         */
        void setCardinalityMinField(String cardinalityMin);

        /**
         * Return the UI element of the 'Cardinality Max' field.
         *
         * @return the UI element of the 'Cardinality Max' field
         */
        WebElement getCardinalityMaxField();

        /**
         * set the UI element of the 'Cardinality Max' field by the given text
         *
         * @param cardinalityMax
         */
        void setCardinalityMaxField(String cardinalityMax);

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

        /**
         * Set the UI element of the 'Definition' field
         *
         * @param newDefinition
         */
        void setDefinition(String newDefinition);

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
         * Return the UI element of the 'Property Term' field.
         *
         * @return the UI element of the 'Property Term' field
         */
        WebElement getPropertyTermField();

        /**
         * Return the UI element of the 'Cardinality Min' field.
         *
         * @return the UI element of the 'Cardinality Min' field
         */
        WebElement getCardinalityMinField();

        /**
         * set the UI element of the 'Cardinality Min' field by the given text.
         *
         * @param cardinalityMin
         */
        void setCardinalityMinField(String cardinalityMin);

        /**
         * Return the UI element of the 'Cardinality Max' field.
         *
         * @return the UI element of the 'Cardinality Max' field
         */
        WebElement getCardinalityMaxField();

        /**
         * set the UI element of the 'Cardinality Max' field by the given text.
         *
         * @param cardinalityMax
         */
        void setCardinalityMaxField(String cardinalityMax);

        /**
         * Return the UI element of the 'Entity Type' select field.
         *
         * @return the UI element of the 'Entity Type' select field
         */
        WebElement getEntityTypeSelectField();

        /**
         * set the UI element of the 'Entity Type' select field by given text.
         *
         * @param entityType Element or Attribute
         */
        void setEntityType(String entityType);

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
         * set the UI element of the 'Value Constraint' select field with the given text
         *
         * @param valueConstraint 'Default Value' or 'Fixed Value' or None
         */
        void setValueConstraint(String valueConstraint);

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
         * set the UI element of the 'Default Value' field with the given text.
         *
         * @param defaultValue
         */
        void setDefaultValue(String defaultValue);

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
         * set the UI element of the 'Definition Source' field with given text
         *
         * @param definitionSource
         */
        void setDefinitionSource(String definitionSource);

        /**
         * Return the UI element of the 'Definition' field.
         *
         * @return the UI element of the 'Definition' field
         */
        WebElement getDefinitionField();

        /**
         * set the UI element of the 'Definition' field with the given text.
         *
         * @param definition
         */
        void setDefinition(String definition);

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
         * Return the UI element of the 'Nillable' checkbox.
         *
         * @return the UI element of the 'Nillable' checkbox
         */
        WebElement getNillableCheckbox();

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

        /**
         * set the UI element of the 'Definition' field with the given text
         *
         * @param definition
         */
        void setDefinition(String definition);

    }

}
