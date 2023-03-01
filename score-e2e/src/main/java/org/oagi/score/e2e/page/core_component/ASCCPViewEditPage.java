package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit ASCCP' page.
 */
public interface ASCCPViewEditPage extends Page {

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
     * Return the ASCCP panel. Use this when the page is opened.
     *
     * @return the ASCCP panel
     */
    ASCCPPanel getASCCPPanel();

    /**
     * Return the ASCCP panel.
     *
     * @param asccpNode ASCCP node
     * @return the ASCCP panel
     */
    ASCCPPanel getASCCPPanel(WebElement asccpNode);

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

    void hitAmendButton();

    void moveToQA();

    void toggleDeprecated();

    void hitUpdateButton();

    void moveToProduction();

    WebElement getMoveToQAButton(boolean enabled);

    WebElement getMoveToProduction(boolean enabled);

    WebElement getDeprecatedCheckbox();

    WebElement getUpdateButton(boolean enabled);
}
