package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit DT' page.
 */
public interface DTViewEditPage extends Page {

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
     * Return the UI element of the 'Data Type Term' field.
     *
     * @return the UI element of the 'Data Type Term' field
     */
    WebElement getDataTypeTermField();

    /**
     * Return the 'Data Type Term' field label text.
     *
     * @return the 'Data Type Term' field label text
     */
    String getDataTypeTermFieldLabel();

    /**
     * Return the value of the 'Data Type Term' field.
     *
     * @return the value of the 'Data Type Term' field
     */
    String getDataTypeTermFieldValue();

    /**
     * Return the UI element of the 'Representation Term' field.
     *
     * @return the UI element of the 'Representation Term' field
     */
    WebElement getRepresentationTermField();

    /**
     * Return the 'Representation Term' field label text.
     *
     * @return the 'Representation Term' field label text
     */
    String getRepresentationTermFieldLabel();

    /**
     * Return the value of the 'Representation Term' field.
     *
     * @return the value of the 'Representation Term' field
     */
    String getRepresentationTermFieldValue();

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

    void showValueDomain();

    WebElement getShowValueDomain();

    WebElement getAddValueDomainButton();

    void addCodeListValueDomain(String name);

    WebElement getTheLastTableRecord();

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void hitUpdateButton();

    void hitUpdateAnywayButton();

    WebElement getUpdateAnywayButton();

    WebElement getUpdateButton(boolean enabled);

    void setQualifier(String qualifier);

    WebElement getQualifierField();

    void codeListIdMarkedAsDeleted(CodeListObject codeList);

    void changeCodeListValueDomain(String codeListName);

    void setDefaultValueDomain(String name);

    WebElement getDefaultValueDomainField();

    void setNamespace(NamespaceObject namespace);

    AddCommentDialog hitAddCommentButton(String path);

    WebElement clickOnDropDownMenuByPath(String path);

    WebElement goToNode(String path);

    WebElement getSearchInputTextField();

    WebElement getContextMenuIconByNodeName(String nodeName);

    WebElement getReviseButton();

    void hitRestoreButton();

    WebElement getRestoreButton();

    String getBasedDataTypeFieldValue();

    WebElement getBasedDataTypeField();

    String getQualifierFieldValue();

    WebElement getSixHexadecimalIdentifierField();

    String getContentComponentDefinitionFieldValue();

    WebElement getContentComponentDefinitionField();

    void setDefinition(String definition);

    void setDefinitionSource(String definitionSource);

    void setContentComponentDefinition(String contentComponentDefinition);

    WebElement getValueDomainByTypeNameAndXSDExpression(String valueDomainType, String valueDomainName, String XSDExpression);


    WebElement getCheckboxForValueDomainByTypeAndName(String valueDomainType, String valueDomainName);

    void discardValueDomain();

    WebElement getDiscardValueDomainButton();

    String getDefinitionWarningDialogMessage();

    WebElement getTableRecordByValue(String value);

    void selectValueDomain(String name);

    void addSupplementaryComponent(String path);

    WebElement getNodeByPath(String path);

    WebElement getMoveToCandidate(boolean enabled);

    void moveToCandidate();

    WebElement getBackToWIPButton(boolean enabled);

    void backToWIP();

    WebElement getDeleteButton();

    void hitDeleteButton();

    WebElement getDeleteAnywayButton();

    DTViewEditPage.SupplementaryComponentPanel getSCPanel(WebElement scNode);

    void removeSupplementaryComponent(String path);

    void hitReviseButton();

    String getDefaultValueDomainFieldValue();

    WebElement getMoveToDraft(boolean enabled);

    void moveToDraft();

    String getDeleteWarningDialogMessage();

    void hitDeleteAnywayButton();

    WebElement getAmendButton();

    void hitAmendButton();

    WebElement getMoveToQAButton(boolean enabled);

    void moveToQA();

    WebElement getMoveToProductionButton(boolean enabled);

    void moveToProduction();

    String getInvalidStateIconText(WebElement node);

    interface SupplementaryComponentPanel {
        void setCardinality(String cardinality);

        WebElement getCardinalityField();

        void setValueConstraintType(String valueConstraintType);

        WebElement getValueConstraintTypeField();

        void setValueConstraint(String valueConstraint);

        WebElement getValueConstraintField();

        String getCardinalityFieldValue();

        String getValueConstraintTypeFieldValue();

        String getValueConstraintFieldValue();

        String getObjectClassTermFieldValue();

        WebElement getObjectClassTermField();

        String getDefinitionFieldValue();

        String getDefinitionSourceFieldValue();

        void setDefinition(String definition);

        String getPropertyTermFieldValue();

        WebElement getPropertyTermField();

        WebElement getRepresentationSelectField();

        WebElement getDefinitionField();

        WebElement getDefinitionSourceField();

        void selectRepresentationTerm(String representationTerm);

        WebElement getTableRecordByValue(String value);

        String getDefaultValueDomainFieldValue();

        void showValueDomain();

        void setPropertyTerm(String propertyTerm);

        void setDefaultValueDomain(String valueDomain);

        WebElement getDefaultValueDomainField();

        void setDefinitionSource(String definitionSource);

        String getRepresentationSelectFieldValue();
    }
}
