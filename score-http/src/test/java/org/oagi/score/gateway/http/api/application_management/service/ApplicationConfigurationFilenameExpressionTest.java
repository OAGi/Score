package org.oagi.score.gateway.http.api.application_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.application_management.controller.payload.FilenameExpressionPreviewResponse;
import org.oagi.score.gateway.http.api.application_management.controller.payload.FilenameExpressionSuggestResponse;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameExpressionSuggester;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameExpressionValidationService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationConfigurationFilenameExpressionTest {

    private final ApplicationConfigurationQueryService service = new ApplicationConfigurationQueryService();

    @BeforeEach
    void wire() {
        ReflectionTestUtils.setField(service, "filenameExpressionValidationService", new FilenameExpressionValidationService());
        ReflectionTestUtils.setField(service, "filenameExpressionSuggester", new FilenameExpressionSuggester());
    }

    @Test
    void thePreviewWithoutSamplesUsesAndReturnsTheDefaults() {
        FilenameExpressionPreviewResponse response = service.previewFilenameExpression(
                null, "bie-schema", "{BIE Property Term:separator('-')}", "-{Incremental}", null, null);

        assertEquals("Item-Master", response.sampleFilename());
        assertEquals("Item-Master-1", response.sampleDuplicateFilename());
        assertEquals("Item Master", response.sampleValues().get("BIE Property Term"));
        assertEquals(List.of("Default Context", "Secondary Context", "Third Context"), response.sampleBusinessContextNames());
    }

    @Test
    void thePreviewAppliesTheSuppliedSamples() {
        FilenameExpressionPreviewResponse response = service.previewFilenameExpression(
                null, "bie-package-schema", "{BIE Package Name}_{Business Context Name[1]}", "",
                Map.of("BIE Package Name", "Pkg"), List.of("A", "B"));

        assertEquals("Pkg_B", response.sampleFilename());
        assertEquals("Pkg", response.sampleValues().get("BIE Package Name"));
        assertEquals(List.of("A", "B"), response.sampleBusinessContextNames());
    }

    @Test
    void thePreviewTypeIsCaseInsensitiveAndAnUnknownTypeIsRejected() {
        assertEquals("1001", service.previewFilenameExpression(
                null, " BIE-Schema ", "{BIE ID}", "", null, null).sampleFilename());
        assertThrows(IllegalArgumentException.class, () -> service.previewFilenameExpression(
                null, "nope", "{BIE ID}", "", null, null));
    }

    @Test
    void anInvalidExpressionIsRejectedByThePreview() {
        assertThrows(IllegalArgumentException.class, () -> service.previewFilenameExpression(
                null, "bie-schema", "{Nope}", "", null, null));
    }

    @Test
    void suggestionsFollowTheTypeAndTheDuplicateHandlerFlag() {
        List<String> schema = labels(service.suggestFilenameExpression(null, "bie-schema", "{BIE Package", null, false));
        List<String> pkg = labels(service.suggestFilenameExpression(null, "bie-package-schema", "{BIE Package", null, false));
        List<String> duplicate = labels(service.suggestFilenameExpression(null, "bie-schema", "{", null, true));

        assertTrue(schema.isEmpty());
        assertEquals(3, pkg.size());
        assertEquals(List.of("Incremental", "BIE ID"), duplicate);
    }

    @Test
    void theCaretDefaultsToTheEndAndTheRangeIsReturned() {
        FilenameExpressionSuggestResponse atEnd = service.suggestFilenameExpression(
                null, "bie-schema", "{BIE Pro", null, false);
        FilenameExpressionSuggestResponse inMiddle = service.suggestFilenameExpression(
                null, "bie-schema", "{BIE Pro}", 8, false);

        assertEquals(1, atEnd.from());
        assertEquals(8, atEnd.to());
        assertEquals(labels(atEnd), labels(inMiddle));
        assertEquals(List.of("BIE Property Term"), labels(atEnd));
    }

    @Test
    void anOutOfRangeCaretAndNullTextAreHandled() {
        assertEquals(List.of(), labels(service.suggestFilenameExpression(null, "bie-schema", "ab", 99, false)));
        assertTrue(labels(service.suggestFilenameExpression(null, "bie-schema", null, null, false)).size() > 0);
        assertThrows(IllegalArgumentException.class, () -> service.suggestFilenameExpression(
                null, "nope", "{", null, false));
    }

    private static List<String> labels(FilenameExpressionSuggestResponse response) {
        return response.items().stream().map(FilenameExpressionSuggestResponse.Item::label).toList();
    }
}
