package org.oagi.score.gateway.http.api.application_management.controller.payload;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FilenameExpressionPreviewRequest {

    private String expression;
    private String duplicateHandlerExpression;
    /** Optional overrides keyed by placeholder name; absent keys use the defaults. */
    private Map<String, String> sampleValues;
    /** Optional ordered Business Context names; absent uses the defaults. */
    private List<String> sampleBusinessContextNames;

}
