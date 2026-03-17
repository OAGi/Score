package org.oagi.score.gateway.http.api.application_management.controller.payload;

public record FilenameExpressionPreviewResponse(
        String sampleFilename,
        String sampleDuplicateFilename) {
}
