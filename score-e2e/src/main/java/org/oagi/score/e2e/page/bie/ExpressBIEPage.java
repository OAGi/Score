package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.ArrayList;

/**
 * An interface of 'Express BIE' page.
 */
public interface ExpressBIEPage extends Page {

    void selectBIEForExpression(TopLevelASBIEPObject topLevelASBIEP);

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

    interface JSONSchemaExpressionOptions {

        WebElement getMakeAsAnArrayCheckbox();

        void toggleMakeAsAnArray();

        WebElement getIncludeMetaHeaderCheckbox();

        void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context);

        WebElement getIncludePaginationResponseCheckbox();

        void toggleIncludePaginationResponse(TopLevelASBIEPObject paginationResponseASBIEP, BusinessContextObject context);

    }

    WebElement getOpenAPIExpressionRadioButton();

    OpenAPIExpressionOptions selectOpenAPIExpression();

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

    void selectJSONOpenAPIFormat();

    enum ExpressionFormat {
        XML,
        JSON,
        YML
    }

    /**
     * Hit the 'Generate' button for a single BIE selection.
     *
     * @param format BIE Expression format
     * @return generate file
     */
    File hitGenerateButton(ExpressionFormat format);

    /**
     * Hit the 'Generate' button.
     *
     * @param format BIE Expression format
     * @param compressed {@code true} if it checks multiple BIEs, otherwise {@code false}
     * @return generate file
     */
    File hitGenerateButton(ExpressionFormat format, boolean compressed);

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

    void selectMultipleBIEsForExpression(ReleaseObject release, ArrayList<TopLevelASBIEPObject> biesForSelection);

    void selectPutEachSchemaInAnIndividualFile();

    WebElement getPutEachSchemaInAnIndividualFileRadioButton();

}
