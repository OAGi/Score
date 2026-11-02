package org.oagi.score.gateway.http.api.application_management.controller.payload;

import java.util.List;
import java.util.Map;

public record FilenameExpressionPreviewResponse(
        String sampleFilename,
        String sampleDuplicateFilename,
        Map<String, String> sampleValues,
        List<String> sampleBusinessContextNames) {
}
