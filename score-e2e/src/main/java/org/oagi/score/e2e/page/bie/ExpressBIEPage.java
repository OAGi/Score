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

    WebElement getDENField();

    void setDEN(String den);

    WebElement getSearchButton();

    void hitSearchButton();

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context);

    void toggleMakeAsAnArray();

    void toggleIncludePaginationResponse(TopLevelASBIEPObject paginationResponseASBIEP, BusinessContextObject context);

    int getNumberOfBIEsInIndexBox();

    int getNumberfBIEsInTable();

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

    void selectXMLSchemaExpression();

    void selectJSONSchemaExpression();

    WebElement getJSONSchemaExpressionRadioButton();

    WebElement getXMLSchemaExpressionRadioButton();

    void selectPutAllSchemasInTheSameFile();

    WebElement getPutAllSchemasInTheSameFileRadioButton();

    WebElement getMakeAsAnArrayCheckbox();

    WebElement getIncludeMetaHeaderCheckbox();

    WebElement getIncludePaginationResponseCheckbox();

    void selectMultipleBIEsForExpression(ReleaseObject release, ArrayList<TopLevelASBIEPObject> biesForSelection);

    void selectPutEachSchemaInAnIndividualFile();

    WebElement getPutEachSchemaInAnIndividualFileRadioButton();

}
