package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.List;
import java.util.function.Function;

/**
 * An interface of 'Express BIE' page.
 */
public interface ExpressBIEPage extends Page {

    void selectBIEForExpression(TopLevelASBIEPObject topLevelASBIEP);

    void selectBIEForExpression(String releaseNum, String topLevelASBIPDEN);

    /**
     * Set the value to the 'Branch' select field.
     *
     * @param branch branch value
     */
    void setBranch(String branch);

    /**
     * Return the UI element of the 'Branch' select field.
     *
     * @return the UI element of the 'Branch' select field
     */
    WebElement getBranchSelectField();

    /**
     * Set the value to the 'State' select field.
     *
     * @param state state value
     */
    void setState(String state);

    /**
     * Return the UI element of the 'State' select field.
     *
     * @return the UI element of the 'State' select field
     */
    WebElement getStateSelectField();

    /**
     * Set the value to the 'Owner' select field.
     *
     * @param owner owner value
     */
    void setOwner(String owner);

    /**
     * Return the UI element of the 'Owner' select field.
     *
     * @return the UI element of the 'Owner' select field
     */
    WebElement getOwnerSelectField();

    WebElement getOpenAPIFormatSelectField();

    WebElement getDENField();

    void setDEN(String den);

    WebElement getSearchButton();

    void hitSearchButton();

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    /**
     * Set the size of items to the 'Items per page' select field.
     *
     * @param items the size of items; 10, 25, 50
     */
    void setItemsPerPage(int items);

    /**
     * Return the total number of items being paged.
     *
     * @return the total number of items being paged
     */
    int getTotalNumberOfItems();

    int getNumberOfBIEsInTable();

    WebElement getXMLSchemaExpressionRadioButton();

    void selectXMLSchemaExpression();

    WebElement getJSONSchemaExpressionRadioButton();

    JSONSchemaExpressionOptions selectJSONSchemaExpression();

    WebElement getOpenAPIExpressionRadioButton();

    OpenAPIExpressionOptions selectOpenAPIExpression();

    void selectJSONOpenAPIFormat();

    /**
     * Hit the 'Generate' button for a single BIE selection.
     *
     * @param format BIE Expression format
     * @return generate file
     */
    File hitGenerateButton(ExpressionFormat format);

    /**
     * Hit the 'Generate' button for a single BIE selection.
     *
     * @param format BIE Expression format
     * @param expectedFilenameMatcher expected file matcher. If it exists, the downloaded file should match the filename with this matcher.
     * @return generate file
     */
    File hitGenerateButton(ExpressionFormat format, Function<String, Boolean> expectedFilenameMatcher);

    /**
     * Hit the 'Generate' button.
     *
     * @param format     BIE Expression format
     * @param compressed {@code true} if it checks multiple BIEs, otherwise {@code false}
     * @return generate file
     */
    File hitGenerateButton(ExpressionFormat format, boolean compressed);

    /**
     * Hit the 'Generate' button.
     *
     * @param format     BIE Expression format
     * @param expectedFilenameMatcher expected file matcher. If it exists, the downloaded file should match the filename with this matcher.
     * @param compressed {@code true} if it checks multiple BIEs, otherwise {@code false}
     * @return generate file
     */
    File hitGenerateButton(ExpressionFormat format, Function<String, Boolean> expectedFilenameMatcher, boolean compressed);

    WebElement getGenerateButton();

    void toggleBIECCTSMetaData();

    WebElement getBIECCTSMetaDataCheckbox();

    void toggleIncludeCCTSDefinitionTag();

    WebElement getIncludeCCTSDefinitionTagCheckbox();

    void toggleBIEGUID();

    WebElement getBIEGUIDCheckbox();

    void toggleBIEOAGIScoreMetaData();

    WebElement getBIEOAGIScoreMetaDataCheckbox();

    void toggleIncludeWHOColumns();

    WebElement getIncludeWHOColumnsCheckbox();

    void toggleBasedCCMetaData();

    void toggleBusinessContext();

    WebElement getBusinessContextCheckbox();

    void toggleBIEDefinition();

    WebElement getBIEDefinitionCheckbox();

    WebElement getBasedCCMetaDataCheckbox();

    void selectPutAllSchemasInTheSameFile();

    WebElement getPutAllSchemasInTheSameFileRadioButton();

    void selectMultipleBIEsForExpression(ReleaseObject release, List<TopLevelASBIEPObject> biesForSelection);

    void selectPutEachSchemaInAnIndividualFile();

    WebElement getPutEachSchemaInAnIndividualFileRadioButton();

    void toggleIncludeBusinessContextInFilename();

    WebElement getIncludeBusinessContextInFilenameCheckbox();

    void toggleIncludeVersionInFilename();

    WebElement getIncludeVersionInFilenameCheckbox();

    enum ExpressionFormat {
        XML,
        JSON,
        YML
    }

    interface JSONSchemaExpressionOptions {

        WebElement getMakeAsAnArrayCheckbox();

        void toggleMakeAsAnArray();

        WebElement getIncludeMetaHeaderCheckbox();

        void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context);

        WebElement getIncludePaginationResponseCheckbox();

        void toggleIncludePaginationResponse(TopLevelASBIEPObject paginationResponseASBIEP, BusinessContextObject context);

    }

    interface OpenAPIExpressionOptions {

        void selectYAMLOpenAPIFormat();

        WebElement getGETOperationTemplateCheckbox();

        OpenAPIExpressionGETOperationOptions toggleGETOperationTemplate();

        WebElement getPOSTOperationTemplateCheckbox();

        OpenAPIExpressionPOSTOperationOptions togglePOSTOperationTemplate();

        void selectJSONOpenAPIFormat();
    }

    interface OpenAPIExpressionGETOperationOptions {

        WebElement getMakeAsAnArrayCheckbox();

        void toggleMakeAsAnArray();

        WebElement getIncludeMetaHeaderCheckbox();

        void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context);

        WebElement getIncludePaginationResponseCheckbox();

        void toggleIncludePaginationResponse(TopLevelASBIEPObject paginationResponseASBIEP, BusinessContextObject context);

    }

    interface OpenAPIExpressionPOSTOperationOptions {

        WebElement getMakeAsAnArrayCheckbox();

        void toggleMakeAsAnArray();

        WebElement getIncludeMetaHeaderCheckbox();

        void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context);

    }

}
