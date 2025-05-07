package org.oagi.score.gateway.http.api.oas_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;

import java.math.BigInteger;

@Data
public class OpenAPITemplateForVerbOption {

    private TopLevelAsbiepId topLevelAsbiepId;
    private TopLevelAsbiepSummaryRecord topLevelAsbiep;
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
