package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;

import java.math.BigInteger;
@Data
public class OpenAPITemplateForVerbOption {
    private String verbOption;
    private boolean arrayForJsonExpression;
    private boolean suppressRootProperty;
    private boolean includeMetaHeader;
    private BigInteger metaHeaderTopLevelAsbiepId;
    private boolean includePaginationResponse;
    private BigInteger paginationResponseTopLevelAsbiepId;

    public OpenAPITemplateForVerbOption(String verbOption) {
        this.verbOption = verbOption;
    }
}
