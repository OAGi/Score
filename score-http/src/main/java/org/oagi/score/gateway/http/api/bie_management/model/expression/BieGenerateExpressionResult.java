package org.oagi.score.gateway.http.api.bie_management.model.expression;

import java.io.File;

public record BieGenerateExpressionResult(String filename,
                                          String contentType,
                                          File file) {
}
