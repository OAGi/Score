package org.oagi.score.gateway.http.api.oas_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;

import java.math.BigInteger;
import java.util.List;

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
    private Integer httpStatusCode;
    private boolean securityOverridden;
    private List<OasSecurityRequirement> securityRequirements;

    // Issue #1347: per-operation error-response body type (PROBLEM_DETAILS | CONFIRM_MESSAGE | NONE,
    // default NONE) and, for CONFIRM_MESSAGE, the picked ConfirmMessage BIE. Keyed by the oas_operation,
    // so the Request-entry and Response-entry templates of one operation carry the same values.
    private String errorResponseBodyType;
    private BigInteger confirmMessageTopLevelAsbiepId;

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
