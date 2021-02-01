package org.oagi.score.gateway.http.api.bie_management.data.expression;

import lombok.Data;

import java.io.File;

@Data
public class BieGenerateExpressionResult {

    private String filename;
    private String contentType;
    private File file;

}
