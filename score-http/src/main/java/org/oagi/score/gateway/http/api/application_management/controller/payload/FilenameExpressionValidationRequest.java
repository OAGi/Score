package org.oagi.score.gateway.http.api.application_management.controller.payload;

import lombok.Data;

@Data
public class FilenameExpressionValidationRequest {

    private String expression;
    private String duplicateHandlerExpression;

}
