package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.List;
import java.util.function.Function;

/**
 * An interface of the {@code Express BIE} page.
 * <p>
 * This page supports generating XML Schema, JSON Schema, OpenAPI, and other
 * expression outputs for one or more selected top-level BIEs. The page object
 * also exposes expression-specific options such as JSON Schema version
 * selection, OpenAPI version and format selection, and schema packaging
 * controls such as separate file references for reused schemas.
 */
public interface ExpressBIEPage extends Page, SearchBarPage {

    /**
     * Select a single top-level BIE record for expression generation.
     *
     * @param topLevelAsbiep the top-level BIE to select
     */
    void selectBIEForExpression(TopLevelASBIEPObject topLevelAsbiep);

    /**
     * Select a single top-level BIE record for expression generation by release
     * number and DEN.
     *
     * @param releaseNum       the release number to search in
     * @param topLevelAsbiepDen the top-level BIE DEN to select
     */
    void selectBIEForExpression(String releaseNum, String topLevelAsbiepDen);

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

    /**
     * Return the UI element of the OpenAPI format select field.
     *
     * @return the UI element of the OpenAPI format select field
     */
    WebElement getOpenAPIFormatSelectField();

    /**
     * Return the UI element of the DEN search field.
     *
     * @return the UI element of the DEN search field
     */
    WebElement getDENField();

    /**
     * Set the value to the DEN search field.
     *
     * @param den DEN value
     */
    void setDEN(String den);

    /**
     * Click the search button and wait for the search result table to finish loading.
     */
    void hitSearchButton();

    /**
     * Return the first table row whose visible text contains the given value.
     *
     * @param value text value to search in the result table
     * @return the matching table row
     */
    WebElement getTableRecordByValue(String value);

    /**
     * Return a specific column element from a table row.
     *
     * @param tableRecord the table row element
     * @param columnName  the logical column name
     * @return the matching column element
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

    /**
     * Return the number of BIE rows currently displayed in the result table.
     *
     * @return the number of displayed BIE rows
     */
    int getNumberOfBIEsInTable();

    /**
     * Return the XML Schema expression radio button.
     *
     * @return the XML Schema expression radio button
     */
    WebElement getXMLSchemaExpressionRadioButton();

    /**
     * Select XML Schema as the expression option.
     */
    void selectXMLSchemaExpression();

    /**
     * Return the JSON Schema expression radio button.
     *
     * @return the JSON Schema expression radio button
     */
    WebElement getJSONSchemaExpressionRadioButton();

    /**
     * Select JSON Schema as the expression option.
     *
     * @return the JSON Schema option accessor for version- and JSON-specific controls
     */
    JSONSchemaExpressionOptions selectJSONSchemaExpression();

    /**
     * Return the OpenAPI expression radio button.
     *
     * @return the OpenAPI expression radio button
     */
    WebElement getOpenAPIExpressionRadioButton();

    /**
     * Select OpenAPI as the expression option.
     *
     * @return the OpenAPI option accessor for version-, format-, and template-specific controls
     */
    OpenAPIExpressionOptions selectOpenAPIExpression();

    /**
     * Return the shared checkbox that controls whether reused schemas are emitted
     * as separate file references for XML Schema and JSON Schema generation.
     *
     * @return the checkbox element for separate file references for reused schemas
     */
    WebElement getSeparateFileReferencesForReusedSchemasCheckbox();

    /**
     * Toggle the {@code Separate file references for reused schemas} option.
     */
    void toggleSeparateFileReferencesForReusedSchemas();

    /**
     * Select JSON as the OpenAPI output format.
     */
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

    /**
     * Return the Generate button.
     *
     * @return the Generate button
     */
    WebElement getGenerateButton();

    void toggleBIECCTSMetaData();

    WebElement getBIECCTSMetaDataCheckbox();

    void toggleIncludeCCTSDefinitionTag();

    WebElement getIncludeCCTSDefinitionTagCheckbox();

    void toggleBIEGUID();

    WebElement getBIEGUIDCheckbox();

    void toggleBIEOAGIConnectCenterMetaData();

    WebElement getBIEOAGIConnectCenterMetaDataCheckbox();

    void toggleIncludeWHOColumns();

    WebElement getIncludeWHOColumnsCheckbox();

    void toggleBasedCCMetaData();

    void toggleBusinessContext();

    WebElement getBusinessContextCheckbox();

    void toggleBIEDefinition();

    WebElement getBIEDefinitionCheckbox();

    WebElement getBasedCCMetaDataCheckbox();

    /**
     * Select the option that generates all schemas into the same file.
     */
    void selectPutAllSchemasInTheSameFile();

    /**
     * Return the radio button for generating all schemas into the same file.
     *
     * @return the radio button for generating all schemas into the same file
     */
    WebElement getPutAllSchemasInTheSameFileRadioButton();

    /**
     * Select multiple top-level BIEs for expression generation.
     *
     * @param release          the release to search in
     * @param biesForSelection the top-level BIEs to select
     */
    void selectMultipleBIEsForExpression(ReleaseObject release, List<TopLevelASBIEPObject> biesForSelection);

    /**
     * Select the option that generates one schema per output file.
     */
    void selectPutEachSchemaInAnIndividualFile();

    /**
     * Return the radio button for generating one schema per file.
     *
     * @return the radio button for generating one schema per file
     */
    WebElement getPutEachSchemaInAnIndividualFileRadioButton();

    /**
     * Toggle the option that includes the business context in generated filenames.
     */
    void toggleIncludeBusinessContextInFilename();

    /**
     * Return the checkbox for including the business context in generated filenames.
     *
     * @return the checkbox for including the business context in generated filenames
     */
    WebElement getIncludeBusinessContextInFilenameCheckbox();

    /**
     * Toggle the option that includes the version in generated filenames.
     */
    void toggleIncludeVersionInFilename();

    /**
     * Return the checkbox for including the version in generated filenames.
     *
     * @return the checkbox for including the version in generated filenames
     */
    WebElement getIncludeVersionInFilenameCheckbox();

    enum ExpressionFormat {
        XML,
        JSON,
        YML
    }

    interface JSONSchemaExpressionOptions {

        /**
         * Return the JSON Schema version select field.
         *
         * @return the JSON Schema version select field
         */
        WebElement getVersionSelectField();

        /**
         * Select the JSON Schema version, for example {@code 2020-12} or {@code Draft-04}.
         *
         * @param version the JSON Schema version to select
         */
        void selectVersion(String version);

        /**
         * Return the JSON Schema checkbox for separating reused schemas into file references.
         *
         * @return the JSON Schema separate-file-reference checkbox
         */
        WebElement getSeparateFileReferencesForReusedSchemasCheckbox();

        /**
         * Toggle the JSON Schema option that separates reused schemas into file references.
         */
        void toggleSeparateFileReferencesForReusedSchemas();

        /**
         * Return the checkbox for generating the JSON expression as an array.
         *
         * @return the JSON array checkbox
         */
        WebElement getMakeAsAnArrayCheckbox();

        /**
         * Toggle the JSON option that generates the expression as an array.
         */
        void toggleMakeAsAnArray();

        /**
         * Return the checkbox for including a Meta Header BIE in the JSON expression.
         *
         * @return the Include Meta Header checkbox
         */
        WebElement getIncludeMetaHeaderCheckbox();

        /**
         * Toggle the JSON Meta Header option. When enabling it, the given meta
         * header BIE and business context are selected in the corresponding dialog.
         *
         * @param metaHeaderASBIEP the Meta Header BIE to select
         * @param context          the business context used to locate the BIE
         */
        void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context);

        /**
         * Return the checkbox for including a Pagination Response BIE in the JSON expression.
         *
         * @return the Include Pagination Response checkbox
         */
        WebElement getIncludePaginationResponseCheckbox();

        /**
         * Toggle the JSON Pagination Response option. When enabling it, the given
         * Pagination Response BIE and business context are selected in the
         * corresponding dialog.
         *
         * @param paginationResponseASBIEP the Pagination Response BIE to select
         * @param context                  the business context used to locate the BIE
         */
        void toggleIncludePaginationResponse(TopLevelASBIEPObject paginationResponseASBIEP, BusinessContextObject context);

    }

    interface OpenAPIExpressionOptions {

        /**
         * Return the OpenAPI version select field.
         *
         * @return the OpenAPI version select field
         */
        WebElement getVersionSelectField();

        /**
         * Select the OpenAPI version, for example {@code 3.1} or {@code 3.0}.
         *
         * @param version the OpenAPI version to select
         */
        void selectVersion(String version);

        /**
         * Select YAML as the OpenAPI output format.
         */
        void selectYAMLOpenAPIFormat();

        /**
         * Return the checkbox for the GET operation template.
         *
         * @return the GET operation template checkbox
         */
        WebElement getGETOperationTemplateCheckbox();

        /**
         * Toggle the GET operation template and return its option accessor.
         *
         * @return the GET operation template option accessor
         */
        OpenAPIExpressionGETOperationOptions toggleGETOperationTemplate();

        /**
         * Return the checkbox for the POST operation template.
         *
         * @return the POST operation template checkbox
         */
        WebElement getPOSTOperationTemplateCheckbox();

        /**
         * Toggle the POST operation template and return its option accessor.
         *
         * @return the POST operation template option accessor
         */
        OpenAPIExpressionPOSTOperationOptions togglePOSTOperationTemplate();

        /**
         * Select JSON as the OpenAPI output format.
         */
        void selectJSONOpenAPIFormat();
    }

    interface OpenAPIExpressionGETOperationOptions {

        /**
         * Return the checkbox for generating the GET operation response as an array.
         *
         * @return the GET array checkbox
         */
        WebElement getMakeAsAnArrayCheckbox();

        /**
         * Toggle the GET operation array option.
         */
        void toggleMakeAsAnArray();

        /**
         * Return the checkbox for including a Meta Header BIE in the GET operation template.
         *
         * @return the GET Include Meta Header checkbox
         */
        WebElement getIncludeMetaHeaderCheckbox();

        /**
         * Toggle the GET Meta Header option. When enabling it, the given meta
         * header BIE and business context are selected in the corresponding dialog.
         *
         * @param metaHeaderASBIEP the Meta Header BIE to select
         * @param context          the business context used to locate the BIE
         */
        void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context);

        /**
         * Return the checkbox for including a Pagination Response BIE in the GET operation template.
         *
         * @return the GET Include Pagination Response checkbox
         */
        WebElement getIncludePaginationResponseCheckbox();

        /**
         * Toggle the GET Pagination Response option. When enabling it, the given
         * Pagination Response BIE and business context are selected in the
         * corresponding dialog.
         *
         * @param paginationResponseASBIEP the Pagination Response BIE to select
         * @param context                  the business context used to locate the BIE
         */
        void toggleIncludePaginationResponse(TopLevelASBIEPObject paginationResponseASBIEP, BusinessContextObject context);

    }

    interface OpenAPIExpressionPOSTOperationOptions {

        /**
         * Return the checkbox for generating the POST operation request as an array.
         *
         * @return the POST array checkbox
         */
        WebElement getMakeAsAnArrayCheckbox();

        /**
         * Toggle the POST operation array option.
         */
        void toggleMakeAsAnArray();

        /**
         * Return the checkbox for including a Meta Header BIE in the POST operation template.
         *
         * @return the POST Include Meta Header checkbox
         */
        WebElement getIncludeMetaHeaderCheckbox();

        /**
         * Toggle the POST Meta Header option. When enabling it, the given meta
         * header BIE and business context are selected in the corresponding dialog.
         *
         * @param metaHeaderASBIEP the Meta Header BIE to select
         * @param context          the business context used to locate the BIE
         */
        void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context);

    }

}
