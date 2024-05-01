package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;
import org.oagi.score.data.TopLevelAsbiep;

import java.math.BigInteger;

@Data
public class OpenAPITemplateForVerbOption {

    private BigInteger topLevelAsbiepId;
    private TopLevelAsbiep topLevelAsbiep;
    private String schemaName;
    private Operation verbOption;
    private String resourceName;
    private String operationId;
    private String tagName;
    private String messageBodyType;

    private boolean arrayForJsonExpression;
    private boolean suppressRootProperty;

    private boolean includeMetaHeader;
    private BigInteger metaHeaderTopLevelAsbiepId;
    private boolean includePaginationResponse;
    private BigInteger paginationResponseTopLevelAsbiepId;

    public OpenAPITemplateForVerbOption(Operation verbOption) {
        this.verbOption = verbOption;
    }

}
