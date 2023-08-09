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

    void setBranch(String branch);

    WebElement getBranchSelectField();

    WebElement getOpenAPIFormatSelectField();

    WebElement getDENField();

    void setDEN(String den);

    WebElement getSearchButton();

    void hitSearchButton();

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    int getNumberOfBIEsInIndexBox();

    int getNumberfBIEsInTable();

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
