package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * An interface of 'Edit BIE' page.
 */
public interface EditBIEPage extends Page {

    String getObjectClassTermFieldLabel();

    void setNamespace(String namespace);

    void getExtendBIELocallyOptionForNode(String path);

    WebElement getSourceDefinitionField();

    WebElement getDefinitionField();

    WebElement getRevisionField();

    WebElement getBNamespaceSelectField();

    String getDENFieldLabel();

    WebElement getObjectClassTermField();

    SelectAssociationDialog appendPropertyAtLast(String path);

    ACCExtensionViewEditPage extendBIELocallyOnNode(String path);

    WebElement getDENField();

    WebElement getSearchField();

    WebElement getSearchButton();

    void clickOnDropDownMenuByPath(String path);

    ACCExtensionViewEditPage extendBIEGloballyOnNode(String path);

    /**
     * Return the panel for 'Top-Level ASBIEP' node.
     *
     * @return 'Top-Level ASBIEP' node panel
     */
    TopLevelASBIEPPanel getTopLevelASBIEPPanel();

    /**
     * Expand the tree using the given node name.
     *
     * @param nodeName node name
     */
    void expandTree(String nodeName);

    /**
     * Return the UI element of the tree node by the node path.
     *
     * @param path the node path
     * @return the UI element of the tree node
     */
    WebElement getNodeByPath(String path);

    /**
     * @param nodeName
     * @return the UI checkbox element of the tree node
     */
    WebElement getCheckboxByNodeName(String nodeName);

    /**
     * Return {@code true} if the node is deprecated, otherwise {@code false}.
     *
     * @param node the node
     * @return {@code true} if the node is deprecated, otherwise {@code false}
     */
    boolean isDeprecated(WebElement node);

    /**
     * Return the UI element of the 'Settings' icon.
     *
     * @return the UI element of the 'Settings' icon
     */
    WebElement getSettingIcon();

    /**
     * Return the UI element of the 'Hide Cardinality' checkbox.
     *
     * @return the UI element of the 'Hide Cardinality' checkbox
     */
    WebElement getHideCardinalityCheckbox();

    /**
     * Toggle the 'Hide Cardinality' checkbox
     */
    void toggleHideCardinality();

    /**
     * Return the UI element of the 'Hide Unused' checkbox.
     *
     * @return the UI element of the 'HHide Unused' checkbox
     */
    WebElement getHideUnusedCheckbox();

    /**
     * Toggle the 'Hide Unused' checkbox
     */
    void toggleHideUnused();

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

    void moveToQA();

    /**
     * Return the UI element of the 'Back to WIP' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Back to WIP' button
     */
    WebElement getBackToWIPButton(boolean enabled);

    void backToWIP();

    /**
     * Return the UI element of the 'Move to Production' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Move to Production' button
     */
    WebElement getMoveToProductionButton(boolean enabled);

    void moveToProduction();

    String getAtentionDialogMessage();

    void getExtendBIEGloballyOptionForNode(String path);

    ACCExtensionViewEditPage continueToExtendBIEOnNode();

    /**
     * An interface of the panel for 'Top-Level ASBIEP' node.
     */
    interface TopLevelASBIEPPanel {

        /**
         * Return the UI element of the 'Release' field.
         *
         * @return the UI element of the 'Release' field
         */
        WebElement getReleaseField();

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
         * Return the UI elements of the 'Business Context' list.
         *
         * @return the UI elements of the 'Business Context' list
         */
        List<WebElement> getBusinessContextList();

        /**
         * Add a business context.
         *
         * @param businessContext a business context
         */
        void addBusinessContext(BusinessContextObject businessContext);

        /**
         * Add a business context by name.
         *
         * @param businessContextName a business context name
         */
        void addBusinessContext(String businessContextName);

        /**
         * Return the UI element of the 'Business Term' field.
         *
         * @return the UI element of the 'Business Term' field
         */
        WebElement getBusinessTermField();

        /**
         * Set the business term to the 'Business Term' field.
         *
         * @param businessTerm business term
         */
        void setBusinessTerm(String businessTerm);

        /**
         * Return the UI element of the 'Remark' field.
         *
         * @return the UI element of the 'Remark' field
         */
        WebElement getRemarkField();

        /**
         * Set the remark to the 'Remark' field.
         *
         * @param remark remark
         */
        void setRemark(String remark);

        /**
         * Return the UI element of the 'Version' field.
         *
         * @return the UI element of the 'Version' field
         */
        WebElement getVersionField();

        /**
         * Set the version to the 'Version' field.
         *
         * @param version version
         */
        void setVersion(String version);

        /**
         * Return the UI element of the 'Status' field.
         *
         * @return the UI element of the 'Status' field
         */
        WebElement getStatusField();

        /**
         * Set the status to the 'Status' field.
         *
         * @param status status
         */
        void setStatus(String status);

        /**
         * Return the UI element of the 'Context Definition' field.
         *
         * @return the UI element of the 'Context Definition' field
         */
        WebElement getContextDefinitionField();

        /**
         * Set the context definition to the 'Context Definition' field.
         *
         * @param contextDefinition context definition
         */
        void setContextDefinition(String contextDefinition);

        /**
         * Return the UI element of the 'Component Definition' field.
         *
         * @return the UI element of the 'Component Definition' field
         */
        WebElement getComponentDefinitionField();

        /**
         * Return the UI element of the 'Type Definition' field.
         *
         * @return the UI element of the 'Type Definition' field
         */
        WebElement getTypeDefinitionField();

    }

    ASBIEPanel getASBIEPanel(WebElement asccpNode);

    interface ASBIEPanel {

        /**
         * Return the UI element of the 'Used' checkbox.
         *
         * @return the UI element of the 'Used' checkbox
         */
        WebElement getUsedCheckbox();

        void toggleUsed();


        /**
         * Return the UI element of the 'Nillable' checkbox.
         *
         * @return the UI element of the 'Nillable' checkbox
         */
        WebElement getNillableCheckbox();

        void toggleNillable();

        /**
         * Return the UI element of the 'Cardinality Min' field.
         *
         * @return the UI element of the 'Cardinality Min' field
         */
        WebElement getCardinalityMinField();

        void setCardinalityMin(int cardinalityMin);

        /**
         * Return the UI element of the 'Cardinality Max' field.
         *
         * @return the UI element of the 'Cardinality Max' field
         */
        WebElement getCardinalityMaxField();

        void setCardinalityMax(int cardinalityMax);

        /**
         * Return the UI element of the 'Remark' field.
         *
         * @return the UI element of the 'Remark' field
         */
        WebElement getRemarkField();

        void setRemark(String remark);

        /**
         * Return the UI element of the 'Context Definition' field.
         *
         * @return the UI element of the 'Context Definition' field
         */
        WebElement getContextDefinitionField();

        void setContextDefinition(String contextDefinition);

        /**
         * Return the UI element of the 'Association Definition' field.
         *
         * @return the UI element of the 'Association Definition' field
         */
        WebElement getAssociationDefinitionField();

        /**
         * Return the UI element of the 'Component Definition' field.
         *
         * @return the UI element of the 'Component Definition' field
         */
        WebElement getComponentDefinitionField();

        /**
         * Return the UI element of the 'Type Definition' field.
         *
         * @return the UI element of the 'Type Definition' field
         */
        WebElement getTypeDefinitionField();

        WebElement getBusinessTermField();
    }

    BBIEPanel getBBIEPanel(WebElement bccpNode);

    interface BBIEPanel {

        /**
         * Return the UI element of the 'Used' checkbox.
         *
         * @return the UI element of the 'Used' checkbox
         */
        WebElement getUsedCheckbox();

        WebElement getBusinessTermField();

        void toggleUsed();

        /**
         * Return the UI element of the 'Nillable' checkbox.
         *
         * @return the UI element of the 'Nillable' checkbox
         */
        WebElement getNillableCheckbox();

        void toggleNillable();

        /**
         * Return the UI element of the 'Cardinality Min' field.
         *
         * @return the UI element of the 'Cardinality Min' field
         */
        WebElement getCardinalityMinField();

        void setCardinalityMin(int cardinalityMin);

        /**
         * Return the UI element of the 'Cardinality Max' field.
         *
         * @return the UI element of the 'Cardinality Max' field
         */
        WebElement getCardinalityMaxField();

        void setCardinalityMax(int cardinalityMax);

        /**
         * Return the UI element of the 'Remark' field.
         *
         * @return the UI element of the 'Remark' field
         */
        WebElement getRemarkField();

        void setRemark(String remark);

        /**
         * Return the UI element of the 'Example' field.
         *
         * @return the UI element of the 'Example' field
         */
        WebElement getExampleField();

        void setExample(String example);

        WebElement getValueConstraintSelectField();

        /**
         * @param value "None", "Fixed Value", "Default Value"
         * @return
         */
        WebElement getValueConstraintFieldByValue(String value);

        void setValueConstraint(String value);

        /**
         * Return the UI element of the 'Fixed Value' field.
         *
         * @return the UI element of the 'Fixed Value' field
         */
        WebElement getFixedValueField();

        void setFixedValue(String fixedValue);

        /**
         * Return the UI element of the 'Default Value' field.
         *
         * @return the UI element of the 'Default Value' field
         */
        WebElement getDefaultValueField();

        void setDefaultValue(String defaultValue);

        WebElement getValueDomainRestrictionSelectField();

        void setValueDomainRestriction(String valueDomainRestriction);

        WebElement getValueDomainField();

        void setValueDomain(String valueDomain);

        /**
         * Return the UI element of the 'Context Definition' field.
         *
         * @return the UI element of the 'Context Definition' field
         */
        WebElement getContextDefinitionField();

        void setContextDefinition(String contextDefinition);

        /**
         * Return the UI element of the 'Association Definition' field.
         *
         * @return the UI element of the 'Association Definition' field
         */
        WebElement getAssociationDefinitionField();

        /**
         * Return the UI element of the 'Component Definition' field.
         *
         * @return the UI element of the 'Component Definition' field
         */
        WebElement getComponentDefinitionField();

    }

    BBIESCPanel getBBIESCPanel(WebElement bdtScNode);

    interface BBIESCPanel {

        /**
         * Return the UI element of the 'Used' checkbox.
         *
         * @return the UI element of the 'Used' checkbox
         */
        WebElement getUsedCheckbox();

        void toggleUsed();

        /**
         * Return the UI element of the 'Cardinality Min' field.
         *
         * @return the UI element of the 'Cardinality Min' field
         */
        WebElement getCardinalityMinField();

        void setCardinalityMin(int cardinalityMin);

        /**
         * Return the UI element of the 'Cardinality Max' field.
         *
         * @return the UI element of the 'Cardinality Max' field
         */
        WebElement getCardinalityMaxField();

        void setCardinalityMax(int cardinalityMax);

        /**
         * Return the UI element of the 'Business Term' field.
         *
         * @return the UI element of the 'Business Term' field
         */
        WebElement getBusinessTermField();

        void setBusinessTerm(String businessTerm);

        /**
         * Return the UI element of the 'Remark' field.
         *
         * @return the UI element of the 'Remark' field
         */
        WebElement getRemarkField();

        void setRemark(String remark);

        /**
         * Return the UI element of the 'Example' field.
         *
         * @return the UI element of the 'Example' field
         */
        WebElement getExampleField();

        void setExample(String example);

        WebElement getValueConstraintSelectField();

        /**
         * @param value "None", "Fixed Value", "Default Value"
         * @return
         */
        WebElement getValueConstraintFieldByValue(String value);

        void setValueConstraint(String value);

        /**
         * Return the UI element of the 'Fixed Value' field.
         *
         * @return the UI element of the 'Fixed Value' field
         */
        WebElement getFixedValueField();

        void setFixedValue(String fixedValue);

        /**
         * Return the UI element of the 'Default Value' field.
         *
         * @return the UI element of the 'Default Value' field
         */
        WebElement getDefaultValueField();

        void setDefaultValue(String defaultValue);

        WebElement getValueDomainRestrictionSelectField();

        void setValueDomainRestriction(String valueDomainRestriction);

        WebElement getValueDomainField();

        void setValueDomain(String valueDomain);

        /**
         * Return the UI element of the 'Context Definition' field.
         *
         * @return the UI element of the 'Context Definition' field
         */
        WebElement getContextDefinitionField();

        void setContextDefinition(String contextDefinition);

        /**
         * Return the UI element of the 'Component Definition' field.
         *
         * @return the UI element of the 'Component Definition' field
         */
        WebElement getComponentDefinitionField();

    }

}
