package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilenameExpressionPreviewTest {

    private final FilenameExpressionValidationService service = new FilenameExpressionValidationService();

    private String schema(String expression) {
        return service.previewBieSchemaExpression(expression, "", FilenameSampleValues.defaults()).sampleFilename();
    }

    private String schema(String expression, FilenameSampleValues samples) {
        return service.previewBieSchemaExpression(expression, "", samples).sampleFilename();
    }

    @Test
    void multiWordValuesShowTheSeparator() {
        assertEquals("ItemMaster", schema("{BIE Property Term}"));
        assertEquals("Item-Master", schema("{BIE Property Term:separator('-')}"));
    }

    @Test
    void businessContextNamesJoinAllOfTheSamples() {
        // the separator joins the words inside each name as well as the names
        assertEquals("Default+Context+Secondary+Context+Third+Context", schema("{Business Context Names:separator('+')}"));
    }

    @Test
    void anIndexedBusinessContextPicksTheNameAtTheIndex() {
        assertEquals("DefaultContext_SecondaryContext_ThirdContext_",
                schema("{Business Context Name[0]}_{Business Context Name[1]}_{Business Context Name[2]}_{Business Context Name[3]}"));
    }

    @Test
    void anIndexBeyondTheSamplesLeavesAnOptionalGroupOut() {
        assertEquals("a", schema("a(-{Business Context Name[9]})"));
        assertEquals("a-DefaultContext", schema("a(-{Business Context Name[0]})"));
    }

    @Test
    void changedSamplesChangeThePreview() {
        FilenameSampleValues samples = FilenameSampleValues.merge(
                Map.of("BIE Property Term", "Order Line Item"), List.of("One", "Two"));

        assertEquals("Order-Line-Item", schema("{BIE Property Term:separator('-')}", samples));
        assertEquals("One_Two", schema("{Business Context Names:separator('_')}", samples));
        assertEquals("", schema("{Business Context Name[2]}", samples));
    }

    @Test
    void theDuplicateFilenameAppliesTheHandlerToTheSampleBieId() {
        FilenameSampleValues samples = FilenameSampleValues.merge(Map.of("BIE ID", "77"), null);

        assertEquals("ItemMaster~77", service.previewBieSchemaExpression("{BIE Property Term}", "~{BIE ID}", samples)
                .sampleDuplicateFilename());
        assertEquals("ItemMaster-1", service.previewBieSchemaExpression("{BIE Property Term}", "-{Incremental}", samples)
                .sampleDuplicateFilename());
    }

    @Test
    void thePreviewReturnsTheSamplesItUsed() {
        FilenameSampleValues samples = FilenameSampleValues.merge(Map.of("BIE Version", "9"), List.of("Only"));

        FilenameExpressionValidationService.PreviewResult result =
                service.previewBieSchemaExpression("{BIE Version}", "", samples);

        assertEquals("9", result.sampleValues().values().get("BIE Version"));
        assertEquals(List.of("Only"), result.sampleValues().businessContextNames());
    }

    @Test
    void packagePlaceholdersAreOnlyForAPackageExpression() {
        assertThrows(IllegalArgumentException.class, () -> schema("{BIE Package Name}"));
        assertEquals("SamplePackage-v1", service.previewBiePackageSchemaExpression(
                "{BIE Package Name}-{BIE Package Version ID}", "", FilenameSampleValues.defaults()).sampleFilename());
    }

    @Test
    void invalidExpressionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> schema(""));
        assertThrows(IllegalArgumentException.class, () -> schema("{Nope}"));
        assertThrows(IllegalArgumentException.class, () -> schema("{BIE ID?nope}"));
        assertThrows(IllegalArgumentException.class, () -> service.previewBieSchemaExpression(
                "{BIE ID}", "{BIE Version}", FilenameSampleValues.defaults()));
        assertThrows(IllegalArgumentException.class, () -> service.previewBieSchemaExpression(
                "{BIE ID}", "{Incremental?includeVersion}", FilenameSampleValues.defaults()));
    }

    @Test
    void validateAcceptsTheDefaultExpressions() {
        service.validateBieSchemaExpression(ExpressionBasedFilenameStrategy.DEFAULT_BIE_SCHEMA_FILENAME_EXPRESSION,
                ExpressionBasedFilenameStrategy.DEFAULT_BIE_SCHEMA_DUPLICATE_HANDLER_EXPRESSION);
        service.validateBiePackageSchemaExpression(ExpressionBasedFilenameStrategy.DEFAULT_BIE_PACKAGE_SCHEMA_FILENAME_EXPRESSION,
                ExpressionBasedFilenameStrategy.DEFAULT_BIE_PACKAGE_SCHEMA_DUPLICATE_HANDLER_EXPRESSION);
    }
}
