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

    /**
     * Return the UI element of the 'Branch' select field.
     *
     * @return the UI element of the 'Branch' select field
     */
    WebElement getBranchSelectField();

    /**
     * Set the 'Branch' select field with the given text.
     *
     * @param branch Branch
     */
    void setBranch(String branch);

    /**
     * Return the UI element of the 'DEN' field.
     *
     * @return the UI element of the 'DEN' field
     */
    WebElement getDENField();

    /**
     * Set the 'DEN' select field with the given text.
     *
     * @param den DEN
     */
    void setDEN(String den);

    /**
     * Return the UI element of the 'Search' button.
     *
     * @return the UI element of the 'Search' button
     */
    WebElement getSearchButton();

    /**
     * Hit the 'Search' button.
     */
    void hitSearchButton();

    /**
     * Return the UI element of the table record at the given index, which starts from 1.
     *
     * @param idx The index of the table record.
     * @return the UI element of the table record at the given index
     */
    WebElement getTableRecordAtIndex(int idx);

    /**
     * Return the UI element of the table record containing the given value.
     *
     * @param value value
     * @return the UI element of the table record
     */
    WebElement getTableRecordByValue(String value);

    /**
     * Return the UI element of the column of the given table record with the column name.
     *
     * @param tableRecord the table record
     * @param columnName  the column name
     * @return the UI element of the column
     */
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

    void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context);

    void toggleMakeAsAnArray();

    void toggleIncludePaginationResponse(TopLevelASBIEPObject paginationResponseASBIEP, BusinessContextObject context);

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
